package se.enterprise.fikavault.entity;

import se.enterprise.neatio.exception.ResourceNotFoundException;

import java.net.URI;

/**
 * Domain exception thrown when an employee profile cannot be found.
 */
public class EmployeeNotFoundException extends ResourceNotFoundException {

    private static final URI EMPLOYEE_NOT_FOUND_TYPE = URI.create("https://neatio.internal/errors/employee-not-found");
    private static final String TITLE = "Employee Not Found";

    public EmployeeNotFoundException(String employeeId) {
        super(EMPLOYEE_NOT_FOUND_TYPE, TITLE, String.format("Employee with ID '%s' was not found.", employeeId));
        withExtension("employeeId", employeeId);
    }
}
