package com.finetune.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Autowired
	private ResolveShopFromSubdomainInterceptor resolveShopFromSubdomainInterceptor;

	@Autowired
	private ShopAccessInterceptor shopAccessInterceptor;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/uploads/shops/**")
				.addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/shops/");
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// Apply to protected API routes. Adjust the pattern as needed.
		registry.addInterceptor(resolveShopFromSubdomainInterceptor).addPathPatterns("/api/**");
		// Enforce shop membership on shop-specific endpoints like workorders
		registry.addInterceptor(shopAccessInterceptor).addPathPatterns("/api/workorders/**");
	}
}
// package com.finetune.app.config;

// import org.springframework.context.annotation.Configuration;
// import org.springframework.core.io.ClassPathResource;
// import org.springframework.core.io.Resource;
// import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
// import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
// import org.springframework.web.servlet.resource.PathResourceResolver;

// import java.io.IOException;

// @Configuration
// public class WebConfig implements WebMvcConfigurer {

//     @Override
//     public void addResourceHandlers(ResourceHandlerRegistry registry) {
//         registry.addResourceHandler("/**")
//                 .addResourceLocations("classpath:/static/dist/")
//                 .resourceChain(true)
//                 .addResolver(new PathResourceResolver() {
//                     @Override
//                     protected Resource getResource(String resourcePath, Resource location) throws IOException {
//                         Resource requestedResource = location.createRelative(resourcePath);
//                         return requestedResource.exists() && requestedResource.isReadable() ? requestedResource
//                                 : new ClassPathResource("/static/dist/index.html");
//                     }
//                 });
//     }
// }
