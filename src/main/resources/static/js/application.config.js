(function () {
    angular.module("OfficeManagementSystem")
        .config(["$routeProvider", "$locationProvider",
            function ($routeProvider, $locationProvider) {
                $routeProvider
                // home page:
                    .when("/", {
                        redirectTo: "/login"
                    })
                    // login page:
                    .when("/login", {
                        templateUrl: "/static/page/login/login-page.html",
                        controller: "LoginController"
                    })
                    .when("/login/:registrationToken/", {
                        templateUrl: "/static/page/login/login-page.html",
                        controller: "LoginController"
                    })
                    // registration page:
                    .when("/registration", {
                        templateUrl: "/static/page/registration/registration-page.html",
                        controller: "RegistrationController"
                    })
                    .when("/reset", {
                        templateUrl: "/static/page/reset/recover-page.html",
                        controller: "RecoverPasswordController"
                    })
                    .when("/resetPassword/:token", {
                        templateUrl: "/static/page/reset/new-password-page.html",
                        controller: "RecoverPasswordController"
                    })
                    .when("/demo", {
                        templateUrl: "/static/page/demo/secured.html",
                        controller: "DemoController"
                    })
                    .otherwise({
                        templateUrl: "/static/error/404.html"
                    });

                $locationProvider.html5Mode({
                    enabled: true,
                    requireBase: false
                }).hashPrefix("!");
            }]);
})();