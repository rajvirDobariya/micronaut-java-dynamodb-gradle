package micronaut.java.dynamodb.gradle.service;

import jakarta.inject.Singleton;
import micronaut.java.dynamodb.gradle.model.Employee;
import micronaut.java.dynamodb.gradle.model.IdGenerator;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

@Singleton
public class EmployeeService {


    private final IdGenerator idGenerator;
    private final DynamoDbClient dynamoDbClient;

    public EmployeeService(DynamoDbClient dynamoDbClient, IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
        this.dynamoDbClient = dynamoDbClient;
    }

    //Create
    public String save(Employee employee) {
        String id = idGenerator.generate();
        employee.setId(id);
        Map<String, AttributeValue> item = item(employee);

        PutItemResponse putItemResponse = dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName("Employee").item(item(employee))
                .build());
        return id;
    }

    // Get all employees
    public List<Employee> findAllEmployees() {
        List<Employee> allEmployees = new ArrayList<>();

        ScanResponse scanResponse = dynamoDbClient.scan(ScanRequest.builder().tableName("Employee").build());
        List<Map<String, AttributeValue>> items = scanResponse.items();

        for (Map<String, AttributeValue> item : items) {
            Employee employee = mapToEmployee(item);
            allEmployees.add(employee);
        }

        while (scanResponse.hasLastEvaluatedKey()) {
            scanResponse = dynamoDbClient.scan(ScanRequest.builder()
                    .tableName("Employee")
                    .exclusiveStartKey(scanResponse.lastEvaluatedKey())
                    .build());
            items = scanResponse.items();

            for (Map<String, AttributeValue> item : items) {
                Employee employee = mapToEmployee(item);
                allEmployees.add(employee);
            }
        }
        return allEmployees;
    }

    // Get Employee by emp_id
    public Employee getEmployeeById(String empId) {
        GetItemResponse getItemResponse = dynamoDbClient.getItem(GetItemRequest.builder()
                .tableName("Employee")
                .key(Map.of("emp_id", AttributeValue.builder().s(empId).build()))
                .build());

        Map<String, AttributeValue> item = getItemResponse.item();
        Employee employee = mapToEmployee(item);
        return employee;
    }

    public List<Employee> getEmployeesByFirstName(String firstName) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":fn", AttributeValue.builder().s(firstName).build());

        ScanResponse response;
        try {
            response = dynamoDbClient.scan(ScanRequest.builder()
                    .tableName("Employee")
                    .filterExpression("firstName = :fn")
                    .expressionAttributeValues(expressionAttributeValues)
                    .build());
        } catch (DynamoDbException e) {
            // Handle exception
            return Collections.emptyList();
        }

        List<Employee> employees = new ArrayList<>();
        for (Map<String, AttributeValue> item : response.items()) {
            Employee employee = mapToEmployee(item);
            employees.add(employee);
        }
        return employees;
    }

    public List<Employee> getEmployeesByEmail(String email) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":em", AttributeValue.builder().s(email).build());

        ScanResponse response;
        try {
            response = dynamoDbClient.scan(ScanRequest.builder()
                    .tableName("Employee")
                    .filterExpression("email = :em")
                    .expressionAttributeValues(expressionAttributeValues)
                    .build());
        } catch (DynamoDbException e) {
            // Handle exception
            return Collections.emptyList();
        }

        List<Employee> employees = new ArrayList<>();
        for (Map<String, AttributeValue> item : response.items()) {
            Employee employee = mapToEmployee(item);
            employees.add(employee);
        }
        return employees;
    }

    public void updateEmployeeEmailById(String empId, String newEmail) {
        try {
            dynamoDbClient.updateItem(UpdateItemRequest.builder()
                    .tableName("Employee")
                    .key(Map.of("emp_id", AttributeValue.builder().s(empId).build()))
                    .updateExpression("SET email = :newEmail")
                    .expressionAttributeValues(Map.of(
                            ":newEmail", AttributeValue.builder().s(newEmail).build()
                    ))
                    .build());
        } catch (DynamoDbException e) {
            // Handle exception
        }
    }

    public void updateEmployeeDetails(String empId, Employee newEmployee) {
        if (newEmployee != null) {
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            StringBuilder updateExpression = new StringBuilder("SET");

            if (notEmptyOrNull(newEmployee.getFirstName())) {
                updateExpression.append(" firstName = :fn,");
                expressionAttributeValues.put(":fn", AttributeValue.builder().s(newEmployee.getFirstName()).build());
            }

            if (notEmptyOrNull(newEmployee.getLastName())) {
                updateExpression.append(" lastName = :ln,");
                expressionAttributeValues.put(":ln", AttributeValue.builder().s(newEmployee.getLastName()).build());
            }

            if (notEmptyOrNull(newEmployee.getEmail())) {
                updateExpression.append(" email = :em,");
                expressionAttributeValues.put(":em", AttributeValue.builder().s(newEmployee.getEmail()).build());
            }

            if (notEmptyOrNull(newEmployee.getPassword())) {
                updateExpression.append(" password = :pass,");
                expressionAttributeValues.put(":pass", AttributeValue.builder().s(newEmployee.getPassword()).build());
            }

            if (notEmptyOrNull(newEmployee.getCountryCode())) {
                updateExpression.append(" countryCode = :cc,");
                expressionAttributeValues.put(":cc", AttributeValue.builder().s(newEmployee.getCountryCode()).build());
            }

            if (notEmptyOrNull(newEmployee.getPhoneNo())) {
                updateExpression.append(" phoneNo = :pn,");
                expressionAttributeValues.put(":pn", AttributeValue.builder().s(newEmployee.getPhoneNo()).build());
            }

            String finalUpdateExpression = updateExpression.toString().replaceAll(",$", "");

            try {
                dynamoDbClient.updateItem(UpdateItemRequest.builder()
                        .tableName("Employee")
                        .key(Map.of("emp_id", AttributeValue.builder().s(empId).build()))
                        .updateExpression(finalUpdateExpression)
                        .expressionAttributeValues(expressionAttributeValues)
                        .build());
            } catch (DynamoDbException e) {
                // Handle exception
            }
        }
    }

    public void deleteEmployeeById(String empId) {
        try {
            dynamoDbClient.deleteItem(DeleteItemRequest.builder()
                    .tableName("Employee")
                    .key(Map.of("emp_id", AttributeValue.builder().s(empId).build()))
                    .build());
        } catch (DynamoDbException e) {
            // Handle exception
        }
    }

    private boolean notEmptyOrNull(String str) {
        return str != null && !str.isEmpty();
    }

    protected Map<String, AttributeValue> item(Employee employee) {
        Map<String, AttributeValue> item = new HashMap<>();

        AttributeValue idAttr = AttributeValue.builder().s(employee.getId()).build();
        AttributeValue emailAttr = AttributeValue.builder().s(employee.getEmail()).build();
        AttributeValue passwordAttr = AttributeValue.builder().s(employee.getPassword()).build();
        AttributeValue firstNameAttr = AttributeValue.builder().s(employee.getFirstName()).build();
        AttributeValue lastNameAttr = AttributeValue.builder().s(employee.getLastName()).build();
        AttributeValue countryCodeAttr = AttributeValue.builder().s(employee.getCountryCode()).build();
        AttributeValue phoneNoAttr = AttributeValue.builder().s(employee.getPhoneNo()).build();

        item.put("emp_id", idAttr);
        item.put("email", emailAttr);
        item.put("password", passwordAttr);
        item.put("firstName", firstNameAttr);
        item.put("lastName", lastNameAttr);
        item.put("countryCode", countryCodeAttr);
        item.put("phoneNo", phoneNoAttr);

        return item;
    }

    public Employee mapToEmployee(Map<String, AttributeValue> item) {
        Employee employee = new Employee();

        employee.setId(item.get("emp_id").s());
        employee.setEmail(item.get("email").s());
        employee.setPassword(item.get("password").s());
        employee.setFirstName(item.get("firstName").s());
        employee.setLastName(item.get("lastName").s());
        employee.setCountryCode(item.get("countryCode").s());
        employee.setPhoneNo(item.get("phoneNo").s());

        return employee;
    }
}
