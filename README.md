# Shamir's Secret Sharing - Java Implementation

This project implements Shamir's Secret Sharing algorithm to find the constant term (secret) of a polynomial given encoded points.

## Problem Overview

Given an unknown polynomial of degree m:
```
f(x) = a_m * x^m + a_{m-1} * x^{m-1} + ... + a_1 * x + c
```

Where:
- We need to find the constant term `c` (the secret)
- We have `k = m + 1` points needed to solve for the coefficients
- Points are provided in encoded format with different bases

## Solution Approach

The solution uses **Lagrange Interpolation** to reconstruct the polynomial and find the constant term at x=0.

### Key Components:

1. **JSON Parsing**: Custom JSON parser to read test case data
2. **Base Decoding**: Convert values from various bases to decimal
3. **Lagrange Interpolation**: Mathematical algorithm to find the secret

### Algorithm Steps:

1. Parse JSON input to extract points and metadata
2. Decode y-values from their respective bases to decimal
3. Apply Lagrange interpolation formula to find f(0)
4. Return the constant term (secret)

## Files

- `ShamirSecretSharing.java` - Main implementation
- `testcase1.json` - First test case
- `testcase2.json` - Second test case
- `README.md` - This documentation

## Usage

```bash
# Compile the Java program
javac ShamirSecretSharing.java

# Run the program
java ShamirSecretSharing
```

## Test Results

### Test Case 1:
- **Input**: 4 points with k=3 (degree 2 polynomial)
- **Decoded Points**: (1,4), (2,7), (3,12)
- **Secret**: 3

### Test Case 2:
- **Input**: 10 points with k=7 (degree 6 polynomial)
- **First 7 decoded points** used for interpolation
- **Secret**: 79836264049851

## Mathematical Foundation

The Lagrange interpolation formula for finding f(0):

```
f(0) = Σ(i=0 to n-1) yi * Li(0)
```

Where Li(0) is the Lagrange basis polynomial:
```
Li(0) = Π(j≠i) (0 - xj) / (xi - xj)
```

## Key Features

- **BigInteger Support**: Handles large numbers up to 256-bit
- **Multiple Base Support**: Decodes values from bases 2-16
- **No External Dependencies**: Pure Java implementation
- **Robust Error Handling**: Graceful handling of file I/O errors

## Verification

The results can be manually verified by:
1. Substituting the found secret and points into the polynomial
2. Checking that all points satisfy the equation
3. Confirming the polynomial degree matches k-1