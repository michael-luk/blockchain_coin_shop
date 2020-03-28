(function ($) {
    $.fn.extend({
        BSAlert: function (title, message, options) {

            var alertTypes = ["success", "info", "warning", "danger"];
            var autoDismiss = true;
            var duration = 2000;

            var alertContainer = document.createElement("DIV");
            alertContainer.classList.add('alert', 'alert-dismissible', 'fade', 'in');
            alertContainer.style.margin = "0";
            alertContainer.style.display = "none";

            var button = document.createElement("BUTTON");
            button.classList.add("close");
            button.setAttribute("type", "button");
            button.setAttribute("data-dismiss", "alert");
            button.innerHTML = '&times;';
            alertContainer.appendChild(button);

            if (title) {
                var titleDiv = document.createElement("P");
                var strongDiv = document.createElement("STRONG");
                var titleText = document.createTextNode(title);
                strongDiv.appendChild(titleText);
                titleDiv.appendChild(strongDiv);

                alertContainer.appendChild(titleDiv);
            }

            var messageText = document.createTextNode(message);
            alertContainer.appendChild(messageText);

            if (options) {
                if (options.type && $.inArray(options.type, alertTypes)) {
                    alertContainer.classList.add("alert-" + options.type);
                } else {
                    alertContainer.classList.add("alert-success");
                }

                if (options.href) {
                    var anchorTag = document.createElement("A");
                    anchorTag.classList.add("alert-link");
                    anchorTag.setAttribute("href", options.href.link);

                    if (options.href.target) {
                        anchorTag.setAttribute("target", options.href.target);
                    }

                    var anchorText = document.createTextNode(options.href.text);
                    anchorTag.appendChild(anchorText);
                    alertContainer.appendChild(anchorTag);
                }

                if (options.display) {
                    var _pos = options.display;
                    alertContainer.style.position = _pos.position;
                    alertContainer.style.width = "350px";
                    alertContainer.style.zIndex = "999999";
                    alertContainer.style.bottom = "20px";
                    alertContainer.style.right = "20px";

                    if (_pos.top){
                        alertContainer.style.top = _pos.top;
                        alertContainer.style.bottom = "inherit";
                    }
                    if (_pos.right)
                        alertContainer.style.right = _pos.right;
                    if (_pos.bottom)
                        alertContainer.style.bottom = _pos.bottom;
                    if (_pos.left){
                        alertContainer.style.left = _pos.left;
                        alertContainer.style.right = "inherit";
                    }
                }

                if (options.hasOwnProperty('autoClose')) {
                    autoDismiss = options.autoClose;
                    console.log("Auto Close");
                }

                if (options.duration) {
                    duration = options.duration;
                }

            } else {
                alertContainer.classList.add("alert-success");
            }

            var closeBSAlert = function () {
                $(alertContainer).slideUp('normal', function () {
                    $(alertContainer).alert('close');
                });
            };

            $(alertContainer).appendTo(this).slideDown('normal', function () {
                if (autoDismiss) setTimeout(closeBSAlert, duration);
            });

        }
    });
})(jQuery);
