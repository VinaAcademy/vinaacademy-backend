package com.vinaacademy.platform.feature.log.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LogConstants {
    public static final String CATEGORY_KEY = "category";
    public static final String PRODUCT_KEY = "product";
    public static final String ORDER_KEY = "order";
    public static final String AUTH_KEY = "authentication";
    public static final String USER_KEY = "user";
    public static final String ROLE_KEY = "role";


    public static final String ADD_ACTION = "add";
    public static final String UPDATE_ACTION = "update";
    public static final String DELETE_ACTION = "delete";
    public static final String DELETE_ALL_ACTION = "delete_all";
    public static final String LOGIN_ACTION = "login";
    public static final String REGISTER_ACTION = "register";
    public static final String LOGOUT_ACTION = "logout";
    public static final String ASSIGN_PASSWORD_ACTION = "assign_password";
    public static final String CHANGE_PASSWORD_ACTION = "change_password";

    public static final String RESEND_VERIFY_EMAIL_ACTION = "resend_verify_email";
    public static final String VERIFY_ACCOUNT_ACTION = "verify_account";
    public static final String FORGOT_PASSWORD_ACTION = "forgot_password";
    public static final String RESET_PASSWORD_ACTION = "reset_password";
}
