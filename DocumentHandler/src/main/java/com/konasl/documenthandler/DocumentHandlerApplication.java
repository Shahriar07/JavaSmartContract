package com.konasl.documenthandler;

import com.konasl.documenthandler.auth.AuthUser;
import com.konasl.documenthandler.configuration.SimpleCORSFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import({SimpleCORSFilter.class})

@SpringBootApplication
public class DocumentHandlerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocumentHandlerApplication.class, args);
	}

}
