package df.open.restypass.testclient.service;

import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by darrenfu on 17-6-24.
 */
@RequestMapping(value = "/app")
public interface ApplicationService {


    void applicationIndex();
}
