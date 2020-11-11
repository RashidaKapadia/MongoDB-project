package ca.utoronto.utm.mcs;

import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = ApiModule.class)
public interface ApiComponent {

    public PostEndPoints buildEndPoints();

}
