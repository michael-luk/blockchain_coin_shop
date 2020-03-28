var app = angular.module('PhoneBindApp', []);

app.controller('PhoneBindController', [
    '$scope',
    '$http',
    function ($scope, $http) {
        $scope.phoneNumber = ''
        $scope.smsNotPass = true

        var InterValObj; //timer变量，控制时间
        var count = 60; //间隔函数，1秒执行
        var curCount;//当前剩余秒数
        var send_code = ""; //验证码
        var codeLength = 4;//验证码长度

        //获取验证码
        $scope.getSmsCaptcha = function () {
            if (!$scope.phoneNumber) {
                alert('请输入手机号码!')
                return
            }

            curCount = count;
            //设置button效果，开始计时
            $(".m_code_btn").css({"background-color": "#999"});
            $("#btnSendCode").attr("disabled", "true");
            $("#btnSendCode").val(curCount + "秒内输入");
            InterValObj = window.setInterval(SetRemainTime, 1000); //启动计时器，1秒执行一次

            //向后台发送处理数据
            var postItem = {phone: $scope.phoneNumber};
            $http({
                method: 'POST',
                url: '/sms/captcha/get',
                data: postItem
            }).success(function (data, status, headers, config) {
                if (data.flag) {
                    $("#smsInfo").html("验证码已经发往手机: " + $scope.phoneNumber);
                } else {
                    $("#smsInfo").html(data.message);
                }
            });
        };

        //timer处理函数
        function SetRemainTime() {
            if (curCount == 0) {
                window.clearInterval(InterValObj);//停止计时器
                $("#btnSendCode").removeAttr("disabled");//启用按钮
                $("#btnSendCode").val("重新发送");
                //code = ""; //清除验证码。如果不清除，过时间后，输入收到的验证码依然有效
            } else {
                curCount--;
                $("#btnSendCode").val(curCount + "秒内输入");
            }
        }

        // ajax的短信验证码校验
        $scope.$watch('smsCaptcha', function (newValue, oldValue) {
            if (newValue === oldValue) {
                return;
            } // AKA first run
            if (newValue.length >= codeLength) {
                if ($scope.phoneNumber == undefined || $scope.phoneNumber == "") return;
                var postItem = {phone: $scope.phoneNumber, checkCode: newValue};
                $http({
                    method: 'POST',
                    url: '/sms/captcha/check',
                    data: postItem
                }).success(function (data, status, headers, config) {
                    if (data.flag) {
                        $("#smsInfo").html("短信验证成功");
                        $scope.smsNotPass = false
                    } else {
                        $("#smsInfo").html("短信验证失败: " + data.message);
                    }
                });
            }
        });

        //提交绑定
        $scope.doBind = function (uid) {
            if ($scope.smsNotPass) {
                $("#btnBind").val("请先通过短信验证");
                return
            }

            var postItem = {phone: $scope.phoneNumber, sms: $scope.smsCaptcha};
            $http({
                method: 'POST',
                url: '/phone/bind',
                data: postItem
            }).success(function (data, status, headers, config) {
                if (data.flag) {
                    $("#smsInfo").html("手机绑定成功");
                    // location.href = '/p/phone/info'
                } else {
                    $("#smsInfo").html(data.message);
                }
            });
        };
    }]);