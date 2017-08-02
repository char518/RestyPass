package df.open.restypass.lb.server;

import df.open.restypass.base.RestyConst;
import df.open.restypass.util.StringBuilderFactory;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Map;

/**
 * 服务实例
 * Created by darrenfu on 17-6-25.
 */
@Data
public class ServerInstance {

    /**
     * 服务实例ID unique
     */
    private String instanceId;

    /**
     * 服务名称
     */
    private String serviceName;


    /**
     * 主机
     */
    private String host;

    /**
     * 端口
     */
    private int port;

    /**
     * 是否 https
     */
    private Boolean isHttps;

//    /**
//     * url
//     */
//    private Uri uri;
    /**
     * 机房
     */
    private String room;

    /**
     * 是否存活
     */
    private Boolean isAlive;

    /**
     * 实例开始服务时间
     */
    private Date startTime;

    /**
     * 其它属性
     */
    private Map<String, Object> props;


    /**
     * Init server instance.
     *
     * @return server instance
     */
    public ServerInstance initProps() {
        if (this.port <= 0 || StringUtils.isEmpty(this.host)) {
            throw new IllegalArgumentException("host/port of server instance can not be null");
        }
        this.setIsAlive(ObjectUtils.defaultIfNull(this.isAlive, true));
        this.setIsHttps(ObjectUtils.defaultIfNull(this.isHttps, false));
        this.setRoom(ObjectUtils.defaultIfNull(this.room, RestyConst.ROOM_DEFAULT));

        this.setServiceName(ObjectUtils.defaultIfNull(this.serviceName, RestyConst.SERVICE_DEFAULT));
        this.setStartTime(ObjectUtils.defaultIfNull(this.startTime, new Date()));


        if (StringUtils.isEmpty(this.instanceId)) {
            StringBuilder sb = StringBuilderFactory.DEFAULT.stringBuilder();
            sb.append(serviceName);
            sb.append("@");
            sb.append(host);
            sb.append(":");
            sb.append(port);
            this.setInstanceId(sb.toString());
        }

        return this;
    }

    /**
     * Gets prop value.
     *
     * @param <T>          the type parameter
     * @param propKey      the prop key
     * @param defaultValue the default value
     * @return the prop value
     */
    public <T> T getPropValue(String propKey, T defaultValue) {
        if (props == null || props.size() == 0) {
            return defaultValue;
        }
        Object propVal = props.get(propKey);

        if (propVal == null) {
            return defaultValue;
        }
        return (T) propVal;
    }
}
