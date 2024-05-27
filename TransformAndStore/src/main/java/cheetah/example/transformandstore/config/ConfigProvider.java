package cheetah.example.transformandstore.config;

import com.trifork.cheetah.processing.job.Job;
import com.trifork.cheetah.processing.util.ConnectorDirection;
import com.trifork.cheetah.processing.util.ParamHelper;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class ConfigProvider implements Serializable {
    private final transient ParamHelper opensearchParamHelper;
    private final String opensearchIndexBaseName;
    private final String opensearchOptional;

    public ConfigProvider(Job job){
        this.opensearchParamHelper = new ParamHelper(job, new ConnectorDirection[]{}, new String[]{"opensearch"}, new String[]{});
        // This is how to get required parameters and will return error if this is null.
        this.opensearchIndexBaseName = opensearchParamHelper.getRequiredParam("index-base-name");
        // This is how to get an optional parameter and specifying a default.
        this.opensearchOptional = opensearchParamHelper.getParam("optional").orElse("optional-param");
    }
}
