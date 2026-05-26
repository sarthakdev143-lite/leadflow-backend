package com.leadflow.leadflow_backend.model;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import com.leadflow.leadflow_backend.service.LeadService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerMapping;


//Check that id is present and available when a new Lead is created.
@Target({ FIELD, METHOD, ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        validatedBy = LeadIdValid.LeadIdValidValidator.class
)
public @interface LeadIdValid {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class LeadIdValidValidator implements ConstraintValidator<LeadIdValid, String> {

        @Autowired
        private final LeadService leadService;

        private final HttpServletRequest request;

        public LeadIdValidValidator(final LeadService leadService,
                final HttpServletRequest request) {
            this.leadService = leadService;
            this.request = request;
        }

        @Override
        public boolean isValid(final String value, final ConstraintValidatorContext cvContext) {
            @SuppressWarnings("unchecked") final Map<String, String> pathVariables =
                    ((Map<String, String>)request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE));

            if (pathVariables == null || !pathVariables.containsKey("id")) {
                return true;
            }

            String error = null;
            if (value == null) {
                error = "NotNull";
            } else if (leadService.idExists(value)) {
                error = "Exists.lead.id";
            }

            if (error != null) {
                cvContext.disableDefaultConstraintViolation();
                cvContext.buildConstraintViolationWithTemplate("{" + error + "}")
                        .addConstraintViolation();
                return false;
            }
            return true;
        }
    }

}
