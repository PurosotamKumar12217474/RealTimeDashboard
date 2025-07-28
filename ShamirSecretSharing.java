import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShamirSecretSharing {
    
    // Class to represent a point (x, y)
    static class Point {
        BigInteger x;
        BigInteger y;
        
        Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }
    
    // Class to represent test case data
    static class TestCase {
        int n;
        int k;
        Map<String, Map<String, String>> points;
        
        TestCase() {
            points = new HashMap<>();
        }
    }
    
    // Method to decode value from given base to decimal
    public static BigInteger decodeValue(String value, int base) {
        return new BigInteger(value, base);
    }
    
    // Simple JSON parser for our specific format
    public static TestCase parseTestCase(String filename) {
        TestCase testCase = new TestCase();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line.trim());
            }
            
            String json = jsonContent.toString();
            
            // Extract n and k values
            Pattern keysPattern = Pattern.compile("\"keys\"\\s*:\\s*\\{[^}]*\"n\"\\s*:\\s*(\\d+)[^}]*\"k\"\\s*:\\s*(\\d+)[^}]*\\}");
            Matcher keysMatcher = keysPattern.matcher(json);
            if (keysMatcher.find()) {
                testCase.n = Integer.parseInt(keysMatcher.group(1));
                testCase.k = Integer.parseInt(keysMatcher.group(2));
            }
            
            // Extract point data
            Pattern pointPattern = Pattern.compile("\"(\\d+)\"\\s*:\\s*\\{\\s*\"base\"\\s*:\\s*\"(\\d+)\"\\s*,\\s*\"value\"\\s*:\\s*\"([^\"]+)\"\\s*\\}");
            Matcher pointMatcher = pointPattern.matcher(json);
            
            while (pointMatcher.find()) {
                String pointKey = pointMatcher.group(1);
                String base = pointMatcher.group(2);
                String value = pointMatcher.group(3);
                
                Map<String, String> pointData = new HashMap<>();
                pointData.put("base", base);
                pointData.put("value", value);
                testCase.points.put(pointKey, pointData);
            }
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        
        return testCase;
    }
    
    // Convert test case to list of points
    public static List<Point> getPointsFromTestCase(TestCase testCase) {
        List<Point> points = new ArrayList<>();
        
        for (Map.Entry<String, Map<String, String>> entry : testCase.points.entrySet()) {
            BigInteger x = new BigInteger(entry.getKey());
            Map<String, String> pointData = entry.getValue();
            int base = Integer.parseInt(pointData.get("base"));
            String value = pointData.get("value");
            BigInteger y = decodeValue(value, base);
            points.add(new Point(x, y));
        }
        
        // We only need k points for interpolation
        return points.subList(0, Math.min(testCase.k, points.size()));
    }
    
    // Lagrange interpolation to find the constant term (secret)
    public static BigInteger findSecret(List<Point> points) {
        BigInteger secret = BigInteger.ZERO;
        int n = points.size();
        
        // For each point, calculate its contribution to the secret
        for (int i = 0; i < n; i++) {
            BigInteger xi = points.get(i).x;
            BigInteger yi = points.get(i).y;
            
            // Calculate Lagrange basis polynomial Li(0)
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;
            
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    BigInteger xj = points.get(j).x;
                    // Li(0) = product of (0 - xj) / (xi - xj) for all j != i
                    numerator = numerator.multiply(BigInteger.ZERO.subtract(xj));
                    denominator = denominator.multiply(xi.subtract(xj));
                }
            }
            
            // Calculate yi * Li(0) and add to secret
            BigInteger contribution = yi.multiply(numerator);
            secret = secret.add(contribution.divide(denominator));
        }
        
        return secret;
    }
    
    public static void main(String[] args) {
        System.out.println("Shamir's Secret Sharing - Finding the Constant Term");
        System.out.println("====================================================");
        
        // Process Test Case 1
        System.out.println("\nTest Case 1:");
        TestCase testCase1 = parseTestCase("testcase1.json");
        List<Point> points1 = getPointsFromTestCase(testCase1);
        
        if (!points1.isEmpty()) {
            System.out.println("n = " + testCase1.n + ", k = " + testCase1.k);
            System.out.println("Decoded points:");
            for (Point p : points1) {
                System.out.println("  " + p);
            }
            BigInteger secret1 = findSecret(points1);
            System.out.println("Secret (constant term): " + secret1);
        }
        
        // Process Test Case 2
        System.out.println("\nTest Case 2:");
        TestCase testCase2 = parseTestCase("testcase2.json");
        List<Point> points2 = getPointsFromTestCase(testCase2);
        
        if (!points2.isEmpty()) {
            System.out.println("n = " + testCase2.n + ", k = " + testCase2.k);
            System.out.println("Decoded points:");
            for (Point p : points2) {
                System.out.println("  " + p);
            }
            BigInteger secret2 = findSecret(points2);
            System.out.println("Secret (constant term): " + secret2);
        }
        
        System.out.println("\n====================================================");
        System.out.println("Summary:");
        if (!points1.isEmpty()) {
            BigInteger secret1 = findSecret(points1);
            System.out.println("Test Case 1 Secret: " + secret1);
        }
        if (!points2.isEmpty()) {
            BigInteger secret2 = findSecret(points2);
            System.out.println("Test Case 2 Secret: " + secret2);
        }
    }
}