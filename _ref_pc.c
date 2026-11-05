/* =====================================================================
 * pc.c   生产者—消费者问题（多进程 + POSIX 命名信号量 + 文件共享内存）
 * 编译： gcc pc.c -o pc -pthread -lrt
 * 运行： ./pc
 * ===================================================================== */

/* ---------------- 5) 头文件 ---------------- */
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>     /* fork, getpid, unlink */
#include <fcntl.h>      /* open, O_CREAT, O_RDWR, O_TRUNC */
#include <sys/mman.h>   /* mmap, munmap, MAP_SHARED, MAP_FAILED */
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/wait.h>   /* wait */
#include <semaphore.h>  /* sem_open, sem_wait, sem_post, sem_close, sem_unlink */

/* ---------------- 4) 缓冲区：同时最多放 10 个数 ---------------- */
#define BUF_SIZE   10          /* 缓冲区容量，最多同时保存 10 个数 */
#define M          500         /* 生产者依次写入 0,1,...,M */
#define N_CONSUMER 3           /* 消费者进程个数 N，必须 > 1 */

#define EMPTY_SLOT (-1)        /* 该单元为空（消费者取走后置此值＝删除） */
#define END_ITEM   (-2)        /* 结束标记，生产者最后发给每个消费者 */

#define BUF_FILE   "/tmp/pc_buffer"
#define SEM_MUTEX  "/pc_mutex"
#define SEM_EMPTY  "/pc_empty"
#define SEM_FULL   "/pc_full"

/* 共享缓冲区：放在 mmap 出来的共享内存里，父子进程看到的是同一份 */
struct shared_buf {
    int buf[BUF_SIZE];   /* 环形缓冲区本体 */
    int in;              /* 生产者写入位置下标 */
    int out;             /* 消费者读取位置下标 */
};

/* ---------------- 1) 函数原型 ---------------- */
void producer(struct shared_buf *sh, sem_t *empty, sem_t *mutex, sem_t *full);
void consumer(struct shared_buf *sh, sem_t *full,  sem_t *mutex, sem_t *empty);

int main(void)
{
    int fd, i;
    struct shared_buf *sh;
    sem_t *mutex, *empty, *full;
    pid_t pid;

    /* ===== ① 用“文件 + ftruncate + mmap(MAP_SHARED)”建立共享缓冲区 ===== */
    fd = open(BUF_FILE, O_RDWR | O_CREAT | O_TRUNC, 0666);
    if (fd == -1) {
        perror("open");
        exit(EXIT_FAILURE);
    }

    if (ftruncate(fd, sizeof(struct shared_buf)) == -1) {
        perror("ftruncate");
        exit(EXIT_FAILURE);
    }

    sh = (struct shared_buf *)mmap(NULL, sizeof(struct shared_buf),
                                   PROT_READ | PROT_WRITE,
                                   MAP_SHARED, fd, 0);
    if (sh == MAP_FAILED) {
        perror("mmap");
        exit(EXIT_FAILURE);
    }
    close(fd);                  /* 映射已建立，文件描述符可以关闭 */

    sh->in  = 0;
    sh->out = 0;
    for (i = 0; i < BUF_SIZE; i++)      /* 缓冲区初始全为空单元 */
        sh->buf[i] = EMPTY_SLOT;

    /* ===== ② 创建三个命名信号量：mutex=1, empty=10, full=0 ===== */
    sem_unlink(SEM_MUTEX);      /* 清掉上次异常退出可能留下的信号量 */
    sem_unlink(SEM_EMPTY);
    sem_unlink(SEM_FULL);

    mutex = sem_open(SEM_MUTEX, O_CREAT | O_EXCL, 0666, 1);
    empty = sem_open(SEM_EMPTY, O_CREAT | O_EXCL, 0666, BUF_SIZE);
    full  = sem_open(SEM_FULL,  O_CREAT | O_EXCL, 0666, 0);
    if (mutex == SEM_FAILED || empty == SEM_FAILED || full == SEM_FAILED) {
        perror("sem_open");
        exit(EXIT_FAILURE);
    }

    /* ===== ③ 本步重点：fork 出 1 个生产者 + N 个消费者（进程，不是线程） ===== */

    /* 生产者进程 */
    pid = fork();
    if (pid == -1) {
        perror("fork producer");
        exit(EXIT_FAILURE);
    }
    if (pid == 0) {
        producer(sh, empty, mutex, full);   /* 子进程执行生产任务 */
        exit(EXIT_SUCCESS);                 /* 子进程必须 exit，不能落到下面 */
    }

    /* N 个消费者进程 */
    for (i = 0; i < N_CONSUMER; i++) {
        pid = fork();
        if (pid == -1) {
            perror("fork consumer");
            exit(EXIT_FAILURE);
        }
        if (pid == 0) {
            consumer(sh, full, mutex, empty);   /* 子进程执行消费任务 */
            exit(EXIT_SUCCESS);
        }
    }

    /* 父进程：等待 1+N 个子进程全部结束，避免僵尸进程 */
    for (i = 0; i < N_CONSUMER + 1; i++)
        wait(NULL);

    /* ===== ④ 清理资源：信号量、共享内存、磁盘文件 ===== */
    sem_close(mutex);
    sem_close(empty);
    sem_close(full);
    sem_unlink(SEM_MUTEX);
    sem_unlink(SEM_EMPTY);
    sem_unlink(SEM_FULL);

    munmap(sh, sizeof(struct shared_buf));
    unlink(BUF_FILE);

    return 0;
}

/* ---------------- 2) 生产者：依次写入 0,1,...,M ---------------- */
void producer(struct shared_buf *sh, sem_t *empty, sem_t *mutex, sem_t *full)
{
    int i;

    for (i = 0; i <= M; i++) {
        sem_wait(empty);                    /* P(empty)：申请一个空单元 */
        sem_wait(mutex);                    /* P(mutex)：互斥进入缓冲区 */

        sh->buf[sh->in] = i;                        /* 写入整数 i */
        sh->in = (sh->in + 1) % BUF_SIZE;           /* 环形移动写指针 */

        sem_post(mutex);                    /* V(mutex)：离开临界区 */
        sem_post(full);                     /* V(full)：产品数加 1 */
    }

    /* 0..M 写完后，给每个消费者各发一个结束标记，否则消费者会永远阻塞 */
    for (i = 0; i < N_CONSUMER; i++) {
        sem_wait(empty);
        sem_wait(mutex);
        sh->buf[sh->in] = END_ITEM;
        sh->in = (sh->in + 1) % BUF_SIZE;
        sem_post(mutex);
        sem_post(full);
    }
}

/* ---------------- 3) 消费者：读一个、删除、输出“PID:数字” ---------------- */
void consumer(struct shared_buf *sh, sem_t *full, sem_t *mutex, sem_t *empty)
{
    int item;

    while (1) {
        sem_wait(full);                     /* P(full)：有产品才能取 */
        sem_wait(mutex);                    /* P(mutex)：互斥进入缓冲区 */

        item = sh->buf[sh->out];            /* 读出一个数 */
        sh->buf[sh->out] = EMPTY_SLOT;      /* 把它从缓冲区删除（清空该单元） */
        sh->out = (sh->out + 1) % BUF_SIZE; /* 环形移动读指针 */

        if (item != END_ITEM) {
            printf("%d:%d\n", (int)getpid(), item);   /* 输出 PID:数字 */
            fflush(stdout);                            /* 立刻刷出，重定向到文件也不丢 */
        }

        sem_post(mutex);                    /* V(mutex)：离开临界区 */
        sem_post(empty);                    /* V(empty)：空单元加 1 */

        if (item == END_ITEM)               /* 收到结束标记就退出 */
            break;
    }
}
