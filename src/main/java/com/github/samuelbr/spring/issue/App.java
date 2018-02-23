package com.github.samuelbr.spring.issue;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.devtools.restart.classloader.RestartClassLoader;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

public class App implements CommandLineRunner {
	
	private static final Logger LOG = LoggerFactory.getLogger(App.class);
	
	private RestartClassLoader classLoader;
	
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	public void run(String... args) throws Exception {
		URL jarUrl = new URL("http://central.maven.org/maven2/org/springframework/spring-expression/4.3.14.RELEASE/spring-expression-4.3.14.RELEASE.jar");
		classLoader = new RestartClassLoader(App.class.getClassLoader(), new URL[] {jarUrl});
		
		Flowable.range(0, 4)
			.parallel(4)
			.runOn(Schedulers.newThread())
			.doOnNext(this::loadAllClasses)
			.sequential()
			.blockingSubscribe();
	}
	
	private void loadAllClasses(int thread) {
		LOG.info("Start thread {}", thread);
		
		String[] classes = {"org.springframework.expression.spel.ast.OpAnd", "org.springframework.expression.spel.ast.OpDec"};
		for (String className: classes) {
			Class<?> clazz = loadClass(className);
			LOG.info("Class loaded: {}", clazz);
		}
	}
	
	private Class<?> loadClass(String name) {
		try {
			return classLoader.loadClass(name, true);
		} catch (ClassNotFoundException e) {
			LOG.error("Unexpected", e);
			throw new RuntimeException(e);
		}
	}
}
