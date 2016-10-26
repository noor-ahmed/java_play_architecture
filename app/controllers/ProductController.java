package controllers;

import akka.actor.ActorSystem;
import auth.NeedLogin;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import play.i18n.MessagesApi;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import services.ProductService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

/**
 * Created by rownak on 10/25/16.
 */

@Security.Authenticated(NeedLogin.class)
public class ProductController extends Controller {
    private ProductService productService;
    private final MessagesApi messagesApi;

    @Inject
    public ProductController(ProductService productService, MessagesApi messagesApi) {
        this.productService = productService;
        this.messagesApi = messagesApi;
    }

    @Inject
    HttpExecutionContext ec;

    // async product creation using default threadpool
    public CompletionStage<Result> create() {
        return CompletableFuture.supplyAsync(() -> {
            JsonNode jsonNode = null;
            try {
                JsonNode json = request().body().asJson();
                jsonNode = productService.create(json);
            } catch (Exception e) {
                jsonNode = Json.toJson(e.getMessage());
            }
            return ok(jsonNode);
        }, ec.current());
    }

    //async find all products using default threadpool
    public CompletionStage<Result> getAll() {
        return CompletableFuture.supplyAsync(() -> {
            JsonNode jsonNode = productService.getAll();
            return ok(jsonNode);
        }, ec.current());
    }

    //async find product using default threadpool
    public CompletionStage<Result> get(String productId) {
        return CompletableFuture.supplyAsync(() -> {
            JsonNode jsonNode = productService.get(productId);
            return ok(jsonNode);
        }, ec.current());
    }

    // async product delete using default threadpool
    public CompletionStage<Result> delete(String productId) {

        //get lang from http req
        play.i18n.Lang lang = Http.Context.current().lang();

        return CompletableFuture.supplyAsync(() -> {
            productService.delete(productId);
            return ok(messagesApi.get(lang, "deleteSuccess"));
        }, ec.current());
    }

    @Inject
    ActorSystem akka;

    // async product update using custom threadpool
    public CompletionStage<Result> update(String productId) {

        //configure my-context in conf/application.conf and here call that dispatcher
        Executor myExecutor = akka.dispatchers().lookup("my-context");

        return CompletableFuture.supplyAsync(() -> {
            JsonNode jsonNode = null;
            try {
                JsonNode json = request().body().asJson();
                jsonNode = productService.update(json, productId);
            } catch (Exception e) {
                jsonNode = Json.toJson(e.getMessage());
            }
            return ok(jsonNode);
        }, new HttpExecutionContext(myExecutor).current());
    }
}
