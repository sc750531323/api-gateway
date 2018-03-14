import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.TypeReference;
import com.nayunfz.common.http.APIResponse
import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import org.apache.commons.io.IOUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus

import javax.servlet.http.HttpServletRequest

public class ResponseWrapFilter extends ZuulFilter {

    private static Logger logger = LoggerFactory.getLogger(ResponseWrapFilter.class);

    @Override
    public String filterType() {
        return "post";
    }

    @Override
    public int filterOrder() {
        return 999;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {

        RequestContext ctx = RequestContext.getCurrentContext();
        InputStream stream = ctx.getResponseDataStream();
        String body;
        try {
            body = IOUtils.toString(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (body == null) {
            return null;
        }
        String contentType;
        try {
            Object zuulResponse = RequestContext.getCurrentContext().get("zuulResponse");
            Map<String, Object> zr = JSON.parseObject(JSON.toJSONString(zuulResponse), new TypeReference<Map<String, Object>>(){});
            Map<String, String> headers = JSON.parseObject(JSON.toJSONString(zr.get("headers")), new TypeReference<Map<String, String>>(){});
            contentType = JSON.parseArray(headers.get("Content-Type"), String.class).get(0);
        }catch (Exception e){
            ctx.setResponseBody(body);
            return null;
        }

        int httpStatusCode = ctx.getResponseStatusCode();
        switch (httpStatusCode) {
            case HttpStatus.OK.value():
                if (contentType.startsWith("application/json")){
                    JSONObject data = JSON.parseObject(body);
                    APIResponse<JSONObject> response = APIResponse.returnSuccess(data);
                    ctx.setResponseBody(JSON.toJSONString(response));
                }else {
                    ctx.setResponseBody(body);
                }
                break;
            case HttpStatus.INTERNAL_SERVER_ERROR.value():
                ctx.setResponseBody(body);  //body已在ControllerAdvice中包装
                break;
            default:
                APIResponse response = APIResponse.returnFail(httpStatusCode, ctx.getThrowable().getMessage());
                ctx.setResponseBody(JSON.toJSONString(response));
        }

        HttpServletRequest request = ctx.getRequest();
        Enumeration<String> params = request.getParameterNames();
        Map<String, Object> paramValue = new HashMap<>();
        while (params.hasMoreElements()) {
            String name = params.nextElement();
            Object value = request.getParameter(name);
            paramValue.put(name, value);
        }
        long t = System.currentTimeMillis() - Long.valueOf(request.getAttribute("t").toString());
        logger.info("requestURI:{}|scheme:{}|host:{}|User-Agent:{}|httpStatusCode:{}|requestParams:{}|contentType:{}|responseBody:{}|cost:{}ms",
                request.getRequestURI(),
                request.getScheme(),
                request.getRemoteHost(),
                request.getHeader("user-agent"),
                httpStatusCode,
                JSON.toJSONString(paramValue),
                contentType,
                ctx.getResponseBody(), t);
        return null;
    }
}