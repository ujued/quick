package net.apisp.quick.example.api;

import net.apisp.quick.core.http.annotation.Get;
import net.apisp.quick.core.http.annotation.View;
import net.apisp.quick.ioc.annotation.Autowired;
import net.apisp.quick.ioc.annotation.Controller;

import java.util.Map;

@Controller
public class IndexApi {

    @Autowired("quickServer.startTime")
    private Long serverStartTime;

    @Get("/")
    @View
    public String index(Map<String, Object> model){
        model.put("runningTime", (System.currentTimeMillis() - serverStartTime)/1000);
        return "index.html";
    }
}
