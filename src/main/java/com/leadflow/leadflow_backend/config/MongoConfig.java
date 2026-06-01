package com.leadflow.leadflow_backend.config;
import com.leadflow.leadflow_backend.util.MongoOffsetDateTimeReader;
import com.leadflow.leadflow_backend.util.MongoOffsetDateTimeWriter;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.event.ValidatingEntityCallback;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;


@Configuration
@EnableMongoRepositories("com.leadflow.leadflow_backend.repos")
public class MongoConfig {

   @Bean
   public MongoTransactionManager transactionManager(final MongoDatabaseFactory databaseFactory) {
       return new MongoTransactionManager(databaseFactory);
   }

   @Bean
   public ValidatingEntityCallback validatingEntityCallback(
           final LocalValidatorFactoryBean factory) {
       return new ValidatingEntityCallback(factory);
   }

   @Bean
   public MongoCustomConversions mongoCustomConversions() {
       return new MongoCustomConversions(Arrays.asList(
               new MongoOffsetDateTimeWriter(),
               new MongoOffsetDateTimeReader()
               ));
   }

}
