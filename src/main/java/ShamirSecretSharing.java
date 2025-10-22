import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShamirSecretSharing {
    
    public static class Point {
        public BigInteger x;
        public BigInteger y;
        
        public Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }
    
    public static class TestCase {
        public int n;
        public int k;
        public List<Point> points;
        
        public TestCase() {
            points = new ArrayList<>();
        }
    }
    
    /**
     * Convert a string from given base to BigInteger
     */
    public static BigInteger convertFromBase(String value, int base) {
        if (base <= 10) {
            return new BigInteger(value, base);
        } else {
            // For bases > 10, we need to handle alphabetic characters
            BigInteger result = BigInteger.ZERO;
            BigInteger baseBI = BigInteger.valueOf(base);
            
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                int digit;
                
                if (c >= '0' && c <= '9') {
                    digit = c - '0';
                } else if (c >= 'a' && c <= 'z') {
                    digit = c - 'a' + 10;
                } else if (c >= 'A' && c <= 'Z') {
                    digit = c - 'A' + 10;
                } else {
                    throw new IllegalArgumentException("Invalid character in base " + base + ": " + c);
                }
                
                if (digit >= base) {
                    throw new IllegalArgumentException("Digit " + digit + " is not valid for base " + base);
                }
                
                result = result.multiply(baseBI).add(BigInteger.valueOf(digit));
            }
            
            return result;
        }
    }
    
    /**
     * Parse JSON test case from file
     */
    public static TestCase parseTestCase(String filename) throws IOException {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(new FileReader(filename), JsonObject.class);
        
        TestCase testCase = new TestCase();
        
        // Parse keys
        JsonObject keys = jsonObject.getAsJsonObject("keys");
        testCase.n = keys.get("n").getAsInt();
        testCase.k = keys.get("k").getAsInt();
        
        // Parse points
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            
            // Skip the "keys" object
            if ("keys".equals(key)) {
                continue;
            }
            
            try {
                BigInteger x = new BigInteger(key);
                JsonObject pointObj = entry.getValue().getAsJsonObject();
                
                int base = Integer.parseInt(pointObj.get("base").getAsString());
                String value = pointObj.get("value").getAsString();
                
                BigInteger y = convertFromBase(value, base);
                
                testCase.points.add(new Point(x, y));
            } catch (NumberFormatException e) {
                // Skip non-numeric keys
                continue;
            }
        }
        
        return testCase;
    }
    
    /**
     * Calculate the constant term using Lagrange interpolation
     * We only need to evaluate the polynomial at x = 0 to get the constant term
     */
    public static BigInteger findSecret(List<Point> points, int k) {
        // We only need k points for interpolation
        List<Point> selectedPoints = points.subList(0, Math.min(k, points.size()));
        
        BigInteger secret = BigInteger.ZERO;
        
        // Lagrange interpolation at x = 0
        for (int i = 0; i < selectedPoints.size(); i++) {
            Point pi = selectedPoints.get(i);
            
            // Calculate Lagrange basis polynomial Li(0)
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;
            
            for (int j = 0; j < selectedPoints.size(); j++) {
                if (i != j) {
                    Point pj = selectedPoints.get(j);
                    
                    // Li(0) = ∏(0 - xj) / (xi - xj) for j ≠ i
                    // This simplifies to ∏(-xj) / (xi - xj)
                    numerator = numerator.multiply(pj.x.negate());
                    denominator = denominator.multiply(pi.x.subtract(pj.x));
                }
            }
            
            // Add yi * Li(0) to the result
            BigInteger term = pi.y.multiply(numerator).divide(denominator);
            secret = secret.add(term);
        }
        
        return secret;
    }
    
    public static void main(String[] args) {
        try {
            // Process test case 1
            System.out.println("Processing Test Case 1:");
            TestCase testCase1 = parseTestCase("testcase1.json");
            
            System.out.println("n = " + testCase1.n + ", k = " + testCase1.k);
            System.out.println("Decoded points:");
            for (Point point : testCase1.points) {
                System.out.println("  " + point);
            }
            
            BigInteger secret1 = findSecret(testCase1.points, testCase1.k);
            System.out.println("Secret (constant term): " + secret1);
            System.out.println();
            
            // Process test case 2
            System.out.println("Processing Test Case 2:");
            TestCase testCase2 = parseTestCase("testcase2.json");
            
            System.out.println("n = " + testCase2.n + ", k = " + testCase2.k);
            System.out.println("Decoded points:");
            for (Point point : testCase2.points) {
                System.out.println("  " + point);
            }
            
            BigInteger secret2 = findSecret(testCase2.points, testCase2.k);
            System.out.println("Secret (constant term): " + secret2);
            
            // Print final results
            System.out.println("\n=== FINAL RESULTS ===");
            System.out.println("Test Case 1 Secret: " + secret1);
            System.out.println("Test Case 2 Secret: " + secret2);
            
        } catch (IOException e) {
            System.err.println("Error reading test case files: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error processing test cases: " + e.getMessage());
            e.printStackTrace();
        }
    }
}