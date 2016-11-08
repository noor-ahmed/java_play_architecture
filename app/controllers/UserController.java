package controllers;

import authorization.NeedLogin;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;
import services.UserService;

import java.util.List;

/**
 * Created by zenith on 10/25/16.
 */
@Singleton
public class UserController extends Controller {
    private final UserService userService;
    private final FormFactory formFactory;

    @Inject
    public UserController(UserService userService, FormFactory formFactory) {
        this.userService = userService;
        this.formFactory = formFactory;
    }

    public Result create() {
        Form<User> formUser = formFactory.form(User.class).bindFromRequest();
        if (formUser.hasErrors()) {
            JsonNode jsonError = formUser.errorsAsJson();
            return Results.badRequest(jsonError);
        }
        return ok(Json.toJson(userService.create(formUser.get())));
    }

    @Security.Authenticated(NeedLogin.class)
    public Result update() {
        Form<User> userForm = formFactory.form(User.class).bindFromRequest();
        if (userForm.hasErrors()) {
            JsonNode jsonError = userForm.errorsAsJson();
            return ok(jsonError);
        }

        User user = userForm.get();

        // TODO : Doesn't use finally instead of catch. It causes writing invalid HTTP response.
        User updatedUser = userService.update(user);
        return ok(Json.toJson(updatedUser));
    }

    public Result getUserGroupByBranch() {
        List<User> users = userService.getUserGroupByBranch();
        return ok(Json.toJson(users));
    }
}
