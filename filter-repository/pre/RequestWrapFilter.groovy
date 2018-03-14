import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * author: zhuo.shi
 * date: 2017/9/6
 */
public class RequestLogFilter extends ZuulFilter {

    private static Logger logger = LoggerFactory.getLogger(RequestLogFilter.class);

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return -100;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        ctx.getRequest().setAttribute("t", System.currentTimeMillis());     //计算接口花费时间
    }
}
