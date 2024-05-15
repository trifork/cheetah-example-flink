package cheetah.example.externallookup.config;

import com.trifork.cheetah.processing.job.Job;
import com.trifork.cheetah.processing.util.ConnectorDirection;
import com.trifork.cheetah.processing.util.ParamHelper;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class ConfigProvider implements Serializable {
    private final transient ParamHelper paramHelper;
    private final String idServiceUrl;
    private final String idServiceToken;
    private final String idServiceClientId;
    private final String idServiceClientSecret;
    private final String idServiceScope;
    private final String idServiceOptional;

    public ConfigProvider(Job job){
        this.paramHelper = new ParamHelper(job, new ConnectorDirection[]{}, new String[]{"id-service"}, new String[]{});
        // This is how to get required parameters and will return error if this is null.
        this.idServiceUrl = paramHelper.getRequiredParam("url");
        this.idServiceToken = paramHelper.getRequiredParam("token-url");
        this.idServiceClientId = paramHelper.getRequiredParam("client-id");
        this.idServiceClientSecret = paramHelper.getRequiredParam("client-secret");
        this.idServiceScope = paramHelper.getRequiredParam("scope");
        // This is how to get an optional parameter and specifying a default.
        this.idServiceOptional = paramHelper.getParam("optional").orElse("optional-param");
    }
}
