/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessrights.domain.projects.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import fr.cnes.regards.modules.accessrights.domain.projects.Role;

/**
 * Allow to validate the field <code>parentRole</code> of a {@link Role}.
 * <p/>
 * Specifies that the annotated role must:
 * <ul>
 * <li>have a <code>null</code> <code>parentRole</code> if it is the role "PUBLIC" or "INSTANCE_ADMIN" or
 * "PROJECT_ADMIN"</li>
 * <li>have a non <code>null</code> which is not "INSTANCE_ADMIN" or "PROJECT_ADMIN" <code>parentRole</code>
 * otherwise</li>
 * </ul>
 *
 * @author Xavier-Alexandre Brochard
 * @author Sylvain Vissiere-Guerinet
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Constraint(validatedBy = HasValidParentValidator.class)
public @interface HasValidParent {

    String message() default "Role should have a parent role which is native unless it is one of the following roles: PUBLIC, INSTANCE_ADMIN, PROJECT_ADMIN";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}