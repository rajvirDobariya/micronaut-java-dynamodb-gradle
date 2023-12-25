package micronaut.java.dynamodb.gradle.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import micronaut.java.dynamodb.gradle.model.Employee;
import micronaut.java.dynamodb.gradle.service.EmployeeService;

import java.util.List;

@ExecuteOn(TaskExecutors.BLOCKING)
@Controller("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Post
    public Employee save(@Body Employee employee) throws JsonProcessingException {
        String id = employeeService.save(employee);
        return employee;
    }

    @Get
    public List<Employee> findAllEmployees() {
        return employeeService.findAllEmployees();
    }

    @Get("/{empId}")
    public Employee getEmployeeById(@PathVariable String empId) {
        return employeeService.getEmployeeById(empId);
    }

    @Get("/firstName/{firstName}")
    public List<Employee> getEmployeesByFirstName(@PathVariable String firstName) {
        return employeeService.getEmployeesByFirstName(firstName);
    }

    @Get("/email/{email}")
    public List<Employee> getEmployeesByEmail(@PathVariable String email) {
        return employeeService.getEmployeesByEmail(email);
    }


    @Put("/{empId}/newEmail/{newEmail}")
    public void updateEmployeeEmailById(@PathVariable String empId, @PathVariable String newEmail) {
         employeeService.updateEmployeeEmailById(empId, newEmail);
    }

    @Put("/{empId}")
    public void updateEmployeeDetails(@PathVariable String empId, @Body Employee newEmployee) {
        employeeService.updateEmployeeDetails(empId,newEmployee);
    }

    @Delete("/{empId}")
    public void deleteEmployeeDetails(@PathVariable String empId) {
        employeeService.deleteEmployeeById(empId);
    }
}
