package com.consulteer.qrcodehandler;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QRCodeHandlerApplication {

  //change this depending on where do you want qr codes to be saved
  private static final String FOLDER_PATH = "/Users/webops/Desktop/QRCodes/";
  private static final String EXTENSION = ".png";
  private static final int WIDTH = 400;
  private static final int HEIGHT = 400;

  static InputStream is = QRCodeHandlerApplication.class.getClassLoader()
      .getResourceAsStream("files/TOB3.png");

  private static BufferedImage getOverlay(InputStream logo) throws IOException {
    return ImageIO.read(logo);
  }

  private static String generateRandomTitle(Random random, int length) {
    return random.ints(48, 122)
        .filter(i -> (i < 57 || i > 65) && (i < 90 || i > 97))
        .mapToObj(i -> (char) i)
        .limit(length)
        .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
        .toString();
  }

  public static void main(String[] args) throws IOException, WriterException, FontFormatException {
    SpringApplication.run(QRCodeHandlerApplication.class, args);

    UUID uuid = UUID.randomUUID();
    String uuidAsString = uuid.toString();
    var uuidNoDashes = uuidAsString.replace("-", "");

    String str = "www.tearsofbacchus.com/".concat(uuidNoDashes);

    Map<EncodeHintType, ErrorCorrectionLevel> hashMap = new HashMap<>();

    hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);

    QRCodeWriter writer = new QRCodeWriter();
    var bitMatrix = writer.encode(str, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hashMap);

    BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
    BufferedImage overlayImg = getOverlay(is);

    int deltaHeight = qrImage.getHeight() - overlayImg.getHeight();
    int deltaWidth = qrImage.getWidth() - overlayImg.getWidth();

    int serialNumber;
    int serialNumberRange;
    Scanner sc = new Scanner(System.in);
    System.out.println("Do you want custom value for serial number?");
    var input = sc.nextLine();
    if ("yes".equals(input)) {
      System.out.println("Input starting serial number: ");
      serialNumber = sc.nextInt();
      System.out.println("Input serial number max value: ");
      serialNumberRange = sc.nextInt();
    } else {
      serialNumber = 1;
      System.out.println("Input serial number max value: ");
      serialNumberRange = sc.nextInt();
    }

    long start = System.currentTimeMillis();
    do {
      var updatedSerial = "ID ".concat(String.format("%06d", serialNumber));
      BufferedImage combined = new BufferedImage(qrImage.getHeight(), qrImage.getWidth(),
          BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = (Graphics2D) combined.getGraphics();

      g.drawImage(qrImage, 10, 0, null);
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
      g.drawImage(overlayImg, deltaHeight + 2, deltaWidth - 12, null);
      InputStream notoSans = QRCodeHandlerApplication.class.getClassLoader()
          .getResourceAsStream("files/NotoSans-Regular.ttf");
      Font font = Font.createFont(Font.TRUETYPE_FONT, notoSans);
      g.setFont(font);
      g.setFont(g.getFont().deriveFont(25.0f));
      Color textColor = Color.BLACK;
      g.setColor(textColor);
      FontMetrics fm = g.getFontMetrics();
      int startingPosition = HEIGHT - 15;
      g.drawString(updatedSerial, (qrImage.getWidth() - 105) - (fm.stringWidth(updatedSerial) / 2),
          startingPosition);

      ByteArrayOutputStream os = new ByteArrayOutputStream();

      ImageIO.write(combined, "png", os);
      Files.copy(new ByteArrayInputStream(os.toByteArray()),
          Paths.get(FOLDER_PATH + generateRandomTitle(new Random(), 9) + EXTENSION),
          StandardCopyOption.REPLACE_EXISTING);
      serialNumber++;
    } while (serialNumber <= serialNumberRange);
    var end = System.currentTimeMillis();
    var duration = end - start;
    var formatedTime = String.format("%02d:%02d:%02d",
        TimeUnit.MILLISECONDS.toHours(duration),
        TimeUnit.MILLISECONDS.toMinutes(duration) -
            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
        TimeUnit.MILLISECONDS.toSeconds(duration) -
            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    System.out.println("Time elapsed: " + formatedTime);
    System.out.println("QR Generated successfully");
  }
}
