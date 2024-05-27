package lenstra;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigInteger;
import java.util.Random;

public class Lenstra extends JFrame {
    private static final BigInteger TWO = BigInteger.valueOf(2);
    private static final BigInteger THREE = BigInteger.valueOf(3);

    private JTextField inputField;
    private JButton factorizeButton;
    private JTextArea resultArea;

    public Lenstra() {
        super("Lenstra ECM Factorizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 350);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        inputField = new JTextField(20);
        factorizeButton = new JButton("Factorize");
        resultArea = new JTextArea(10, 30);
        resultArea.setWrapStyleWord(true);
        resultArea.setLineWrap(true);
        resultArea.setEditable(false);

        panel.add(new JLabel("Enter a number:"));
        panel.add(inputField);
        panel.add(factorizeButton);

        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        factorizeButton.addActionListener(this::performFactorization);

        setVisible(true);
    }

    private void performFactorization(ActionEvent e) {
        String input = inputField.getText().trim();
        try {
            BigInteger number = new BigInteger(input);
            BigInteger factor = lenstraECM(number, 50); // Set limit to 50 trials
            if (factor != null) {
                resultArea.setText("A nontrivial factor of " + number + " is " + factor);
            } else {
                resultArea.setText("No factor found for " + number);
            }
        } catch (NumberFormatException ex) {
            resultArea.setText("Invalid input. Please enter a valid integer.");
        }
    }

    private BigInteger lenstraECM(BigInteger n, int limit) {
        for (int i = 0; i < limit; i++) {
            BigInteger a = randomBigInteger(n);
            BigInteger x = randomBigInteger(n);
            BigInteger y = randomBigInteger(n);
            BigInteger b = y.pow(2).subtract(x.pow(3)).subtract(a.multiply(x)).mod(n);
            Point p = new Point(x, y);

            BigInteger k = BigInteger.valueOf(2);
            for (int j = 2; j <= 100; j++) {
                k = k.multiply(BigInteger.valueOf(j));
                try {
                    p = multiplyPoint(p, k, a, b, n);
                } catch (ArithmeticException e) {
                    BigInteger gcd = n.gcd(e.getMessage().contains("/ by zero") ? n.subtract(x) : y);
                    if (gcd.compareTo(BigInteger.ONE) > 0) {
                        return gcd;
                    }
                }
            }
        }
        return null; // No factor found within given limit
    }

    private BigInteger randomBigInteger(BigInteger n) {
        Random rand = new Random();
        BigInteger result = new BigInteger(n.bitLength(), rand);
        while (result.compareTo(BigInteger.ZERO) <= 0 || result.compareTo(n) >= 0) {
            result = new BigInteger(n.bitLength(), rand);
        }
        return result;
    }

    private Point multiplyPoint(Point p, BigInteger k, BigInteger a, BigInteger b, BigInteger n) {
        Point result = Point.infinity();
        Point addend = p;

        while (k.compareTo(BigInteger.ZERO) > 0) {
            if (k.mod(TWO).equals(BigInteger.ONE)) {
                result = pointAddition(result, addend, a, b, n);
            }
            addend = pointAddition(addend, addend, a, b, n);
            k = k.shiftRight(1);
        }

        return result;
    }

    private Point pointAddition(Point p1, Point p2, BigInteger a, BigInteger b, BigInteger n) {
        if (p1.isInfinity) return p2;
        if (p2.isInfinity) return p1;

        BigInteger x1 = p1.x, y1 = p1.y;
        BigInteger x2 = p2.x, y2 = p2.y;

        if (x1.equals(x2) && y1.equals(y2)) {
            if (y1.equals(BigInteger.ZERO)) return Point.infinity();

            BigInteger m = THREE.multiply(x1.pow(2)).add(a).multiply(TWO.multiply(y1).modInverse(n)).mod(n);
            BigInteger x3 = m.pow(2).subtract(TWO.multiply(x1)).mod(n);
            BigInteger y3 = m.multiply(x1.subtract(x3)).subtract(y1).mod(n);
            return new Point(x3, y3);
        }

        if (x1.equals(x2)) return Point.infinity();

        BigInteger m = y2.subtract(y1).multiply(x2.subtract(x1).modInverse(n)).mod(n);
        BigInteger x3 = m.pow(2).subtract(x1).subtract(x2).mod(n);
        BigInteger y3 = m.multiply(x1.subtract(x3)).subtract(y1).mod(n);
        return new Point(x3, y3);
    }

    private static class Point {
        public BigInteger x, y;
        public boolean isInfinity;

        public Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
            this.isInfinity = false;
        }

        public static Point infinity() {
            return new Point(BigInteger.ZERO, BigInteger.ZERO) {{ isInfinity = true; }};
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Lenstra::new);
    }
}
