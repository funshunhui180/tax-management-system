/* 税务业务管理系统 · 通用交互 */
(function () {
    "use strict";

    document.addEventListener("DOMContentLoaded", function () {
        var body = document.body;
        var toggle = document.querySelector(".menu-toggle");
        var overlay = document.querySelector(".sidebar-overlay");

        function closeNav() {
            body.classList.remove("nav-open");
        }

        if (toggle) {
            toggle.addEventListener("click", function () {
                body.classList.toggle("nav-open");
            });
        }

        if (overlay) {
            overlay.addEventListener("click", closeNav);
        }

        document.addEventListener("keydown", function (event) {
            if (event.key === "Escape") {
                closeNav();
            }
        });

        document.querySelectorAll(".sidebar .nav-item").forEach(function (link) {
            link.addEventListener("click", function () {
                if (window.matchMedia("(max-width: 960px)").matches) {
                    closeNav();
                }
            });
        });

        // 操作成功提示：4 秒后自动淡出
        document.querySelectorAll(".flash").forEach(function (flash) {
            window.setTimeout(function () {
                flash.classList.add("is-hiding");
                window.setTimeout(function () {
                    flash.remove();
                }, 380);
            }, 4000);
        });
    });
})();
