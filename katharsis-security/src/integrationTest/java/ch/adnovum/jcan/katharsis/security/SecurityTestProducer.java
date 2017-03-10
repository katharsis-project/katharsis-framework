package ch.adnovum.jcan.katharsis.security;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import ch.adnovum.jcan.katharsis.mock.models.Project;
import ch.adnovum.jcan.katharsis.mock.models.Task;
import ch.adnovum.jcan.katharsis.security.SecurityConfig.Builder;

@ApplicationScoped
public class SecurityTestProducer {

	@Produces
	@ApplicationScoped
	public SecurityModule getSecurityModule() {
		Builder builder = SecurityConfig.builder();
		builder.permitRole("allRole", ResourcePermission.ALL);
		builder.permitRole("getRole", ResourcePermission.GET);
		builder.permitRole("patchRole", ResourcePermission.PATCH);
		builder.permitRole("postRole", ResourcePermission.POST);
		builder.permitRole("deleteRole", ResourcePermission.DELETE);
		builder.permitRole("taskRole", Task.class, ResourcePermission.ALL);
		builder.permitRole("taskReadRole", Task.class, ResourcePermission.GET);
		builder.permitRole("projectRole", Project.class, ResourcePermission.ALL);
		builder.permitAll(ResourcePermission.GET);
		builder.permitAll(Project.class, ResourcePermission.POST);
		return SecurityModule.newServerModule(builder.build());
	}

}
