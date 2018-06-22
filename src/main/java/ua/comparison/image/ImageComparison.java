package ua.comparison.image;

import ua.comparison.image.model.Rectangle;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static java.awt.Color.RED;
import static ua.comparison.image.ImageComparisonTools.*;

public class ImageComparison {

    /**
     * The threshold which means the max distance between non-equal pixels.
     * Could be changed according size and requirements to the image.
     */
    public static int threshold = 5;

    /**
     * The number which marks how many rectangles. Beginning from 2.
     */
    private int counter = 2;

    /**
     * The number of the marking specific rectangle.
     */
    private int regionCount = counter;

    private final BufferedImage image1;
    private final BufferedImage image2;
    private int[][] matrix;
    private List<Rectangle> rectangleList = new ArrayList<>();

    ImageComparison(String image1Name, String image2Name) throws IOException, URISyntaxException {
        image1 = readImageFromResources(image1Name);
        image2 = readImageFromResources(image2Name);
        matrix = populateTheMatrixOfTheDifferences(image1, image2);
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        ImageComparison comparison = new ImageComparison("image1.png", "image3.png");
        createGUI(comparison.compareImages());
    }

    /**
     * Draw rectangles which cover the regions of the difference pixels.
     * @return the result of the drawing.
     */
    BufferedImage compareImages() throws IOException, URISyntaxException {
        // check images for valid
        checkCorrectImageSize(image1, image2);

        groupRegions();

        findRectangles();

        BufferedImage outImg = deepCopy(image2);

        drawRectangles(outImg);

        //save the image:
        saveImage("build/result2.png", outImg);

        return outImg;
    }

    private void drawRectangles(BufferedImage image) {
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(RED);

        rectangleList.forEach(rectangle -> graphics.drawRect(rectangle.getMinY(),
                                                              rectangle.getMinX(),
                                                              rectangle.getWidth(),
                                                              rectangle.getHeight()));
    }

    /**
     * Finds all rectangles with different pixels.
     */
    private void findRectangles() {
        if(counter > regionCount) {
            findOverlappingRectangles();
            return;
        }

        rectangleList.add(createRectangle(matrix, counter));

        counter++;

        findRectangles();
    }

    /**
     * Find all overlapping rectangles and union them.
     */
    private void findOverlappingRectangles() {
        // TODO: 23.06.2018 doesn't work. Should be debugged.
        for(int i = 0; i < rectangleList.size(); i ++) {
            for(int j = i + 1; j < rectangleList.size(); j++) {
               if(isOverlap(rectangleList.get(i), rectangleList.get(j))) {
                   Rectangle mergedRectangle = mergeRectangles(rectangleList.get(i), rectangleList.get(j));
                   rectangleList.remove(i);
                   rectangleList.remove(j);
                   rectangleList.add(mergedRectangle);
               }
            }
        }
    }

    public boolean isOverlap(Rectangle rectangle1,
                             Rectangle rectangle2) {

        if(rectangle1.getMinX() > rectangle2.getMaxX() ||
                rectangle2.getMaxX() > rectangle1.getMinX()) {
            return false;
        }

        if(rectangle1.getMinY() < rectangle2.getMaxY() ||
                rectangle2.getMaxY() < rectangle1.getMinY()) {
            return false;
        }

        return true;
    }

    private Rectangle mergeRectangles(Rectangle rectangle1, Rectangle rectangle2) {

        Rectangle mergedRectangle = new Rectangle();

        mergedRectangle.setMinX(rectangle1.getMinX() < rectangle2.getMinX() ? rectangle1.getMinX(): rectangle2.getMinX());
        mergedRectangle.setMinY(rectangle1.getMinY() < rectangle2.getMinY() ? rectangle1.getMinY(): rectangle2.getMinY());
        mergedRectangle.setMinX(rectangle1.getMaxX() < rectangle2.getMaxX() ? rectangle1.getMaxX(): rectangle2.getMaxX());
        mergedRectangle.setMinX(rectangle1.getMaxY() < rectangle2.getMaxY() ? rectangle1.getMaxY(): rectangle2.getMaxY());

        return mergedRectangle;
    }

    /**
     * Group rectangle regions in binary matrix.
     */
    private void groupRegions() {
        for (int row = 0; row < matrix.length; row++) {
            for (int col = 0; col < matrix[row].length; col++) {
                if (matrix[row][col] == 1) {
                    joinToRegion(row, col);
                    regionCount++;
                }
            }
        }
    }

    /**
     * The recursive method which go to all directions and finds difference
     * in binary matrix using {@code threshold} for setting max distance between values which equal "1".
     * and set the {@code groupCount} to matrix.
     * @param row the value of the row.
     * @param col the value of the column.
     */
    private void joinToRegion(int row, int col) {
        if (row < 0 || row >= matrix.length || col < 0 || col >= matrix[row].length || matrix[row][col] != 1) return;

        matrix[row][col] = regionCount;

        for (int i = 0; i < threshold; i++) {
            // goes to all directions.
            joinToRegion(row - 1 - i, col);
            joinToRegion(row + 1 + i, col);
            joinToRegion(row, col - 1 - i);
            joinToRegion(row, col + 1 + i);

            joinToRegion(row - 1 - i, col - 1 - i);
            joinToRegion(row + 1 + i, col - 1 - i);
            joinToRegion(row - 1 - i, col + 1 + i);
            joinToRegion(row + 1 + i, col + 1 + i);
        }
    }
}
