import java.util.Date;

/**
 * 运行耗时计算工具类
 * @Author 胡朴桤
 */
public class RunTimeUtil {
    public long beginTime=0;//开始时间
    public long beforeTime=0;//上一次记录的时间
    public Integer count=0;//调用次数

    public RunTimeUtil(){
        resetTime();
    }

    /**
     * 获取当前时间戳
     * @return
     */
    private long getTime(){
        return new Date().getTime();
    }

    /**
     * 距离上一次调用耗时
     * @return
     */
    public long beforeUseTime(){
        long time=getTime();
        long useTime=time-beforeTime;
        beforeTime=time;
        return useTime;
    }

    /**
     * 输出距离上一次调用耗时
     */
    public void beforeUseTimeOutPrint(){
        count++;
        System.out.println("*****************第"+count+"次调用,距离上一次调用耗时"+beforeUseTime()+"********************");
    }

    /**
     * 总耗时
     */
    public long allUseTime(){
        return getTime()-beginTime;
    }

    /**
     * 输出总共耗时
     */
    public void allUseTimeOutPrint(){
        System.out.println("*******************总共耗时:"+allUseTime()+"***********************");
    }


    /**
     * 重置时间
     */
    public void resetTime(){
        resetBeginTime();
        resetBeforeTime();
    }

    /**
     * 重置开始时间
     */
    public void resetBeginTime(){
        beginTime=getTime();
    }

    /**
     * 重置上一次调用时间
     */
    public void resetBeforeTime(){
        beforeTime=getTime();
    }



}
