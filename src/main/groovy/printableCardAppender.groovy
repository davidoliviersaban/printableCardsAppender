#!/usr/bin/env groovy

import javax.imageio.ImageIO
import javax.print.attribute.standard.MediaSize
import javax.print.attribute.standard.MediaSizeName
import java.awt.image.BufferedImage
import java.awt.Graphics2D

/**
 * This class appends into one image several images added through the method addImg
 * Once the image is full or no more image is to be added, the image can be output as a PNG file
 */
class ImageAppender {

  static final int MARGIN = 0.25
  static final boolean PORTRAIT = false
  static final int PPI = 300
  static final String A4 = "ISO_A4"
  static final String OUTPUT_FORMAT = "png"


  private BufferedImage img// = new BufferedImage(3508,2480,BufferedImage.TYPE_INT_ARGB)
  // Default margin value
  private def paperFormat, landscape, ppi, margin, outputFormat
  private int computedMargin
  private int currentWidth
  private int currentHeight
  private int maxCopiedHeight = 0
  private int maxCopiedWidth = 0

  /**
   * Transforms a requested string into an actual paper format.
   * @param paperFormat
   * @return
   */
  MediaSizeName findMediaSize(String paperFormat) {
    try {
      MediaSizeName.ISO_A4
      // Paper lookup became obsolete with newer java version
      // int paperFormatId = MediaSizeName.getStringTable().findIndexOf { item -> item.contains(paperFormat) }
      // MediaSizeName.getEnumValueTable()[paperFormatId]
    }
    catch (Exception e) {
      println("[Error] Unknown paper format")
      throw e
    }
  }

  ImageAppender(ImageAppender it) {
    this(it.paperFormat,it.landscape, it.ppi, it.margin, it.outputFormat)
  }

  ImageAppender(String paperFormat = A4, boolean landscape = false, int ppi = PPI, int margin = MARGIN, String outputFormat = OUTPUT_FORMAT) {
    this.paperFormat = paperFormat
    this.landscape = landscape
    this.ppi = ppi
    this.margin = margin
    this.outputFormat = outputFormat
    init()
  }

  private void init() {
    float[] dimensions = MediaSize.getMediaSizeForName(findMediaSize(paperFormat.toString())).getSize(MediaSize.INCH)
    int w = landscape ? dimensions[1] * ppi : dimensions[0] * ppi
    int h = landscape ? dimensions[0] * ppi : dimensions[1] * ppi
    this.img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
    currentHeight = currentWidth = computedMargin = margin * ppi
  }


  // Counts the images created. Increased every time an image is written
  private static int id = 0

  void writeImage(String output) {
    ImageIO.write(centerImage(),
                  outputFormat.toUpperCase(),
                  new File("${output}.${id++}.${outputFormat}"))
  }

  /**
   * Adds/Appends to the image inside the current bigger image
   * @param bi smaller image that should be put into a bigger image
   * @return the image can be added or there is not enough empty space to add this image
   */
  boolean addImg(BufferedImage bi) {
    if (bi.width + currentWidth + computedMargin < img.width) {
      img.createGraphics().drawImage(bi, currentWidth, currentHeight, null)
      maxCopiedHeight = Math.max(maxCopiedHeight, bi.height)
      currentWidth += bi.width
      maxCopiedWidth = Math.max(currentWidth, maxCopiedWidth)
      return true
    } else if (bi.height + currentHeight + maxCopiedHeight + computedMargin < img.height) {
      currentHeight += maxCopiedHeight
      maxCopiedHeight = 0
      currentWidth = computedMargin
      return addImg(bi)
    }
    return false
  }

  BufferedImage centerImage() {
    BufferedImage centerImage = new BufferedImage(this.img.width, this.img.height, this.img.type)
    int xStart = (this.img.width - this.maxCopiedWidth)/2 - this.computedMargin
    Graphics2D g = centerImage.createGraphics()
    g.drawImage(this.img, xStart, 0, null)
    g.dispose()
    return centerImage
  }

}

def usage() {
  "Usage:\n"+
  "printableCardAppender <input_folder> [output_folder paper_formatt isLandscape ppi margin]" +
  "OPTIONS:\n"+
    "\n\tintput_folder\t(mandatory) folder containing all images that should be merged into a A4 format." +
    "\n\toutput_folder\tfolder & filename prefix for the output A4 images. Default value is ./a4image" +
    "\n\tpaper_formatt \tformat of the output image. default format is A4. Most printable formats are supported " +
        "(see javadoc for javax.print.attribute.standard.MediaSizeName)" +
    "\n\tisLandscape  \twhether the image should be in landscape (true) or portrait(false). Default value is false (portrait)" +
    "\n\tppi          \tpixels per inch you wish to get. Default value is 300" +
    "\n\tmargin       \tnumber of inches you wish to keep as margin to print your final image. Default value is 0.25 (= 0.25in ~= 6mm)"
}

def run(args) {
  def input = args[0]
  def output = (args.length > 1) ? args[1] : "a4image"
  def paperFormat = (args.length > 2) ? args[2] : ImageAppender.ISO_A4
  def landscape = (args.length > 3) ? Boolean.parseBoolean(args[3]) : ImageAppender.PORTRAIT
  def ppi = (args.length > 4) ? Integer.parseInt(args[4]) : ImageAppender.PPI
  def margin = (args.length > 5) ? Integer.parseInt(args[5]) : ImageAppender.MARGIN

  if (new File(output).getParentFile() != null) {
    new File(output).getParentFile().mkdirs()
  }
  else {
    println("[INFO] No parent directory can be found, generating files on local directory: ./")
  }


  File path = new File(input)

  if (!path.isDirectory()) {
    println("[Error] input must be a folder")
    println(usage())
    return
  }

  // Compatibility change in format names appending ISO_ before paper format name
  def matcher = paperFormat =~ /(^[ABC][0-9]+$)/
  if (matcher.size() == 1) {
    paperFormat = 'ISO_' + paperFormat
  }

  println("Input summary is paper_format: ${paperFormat}, landscape: ${landscape}, ppi: ${ppi}, margin: ${margin}")

  ImageAppender imgAppender = new ImageAppender(paperFormat, landscape, ppi, margin)

  path.listFiles().sort().each{ file ->
    if (file.isFile()) { //this line weeds out other directories/foldersB
      BufferedImage img = ImageIO.read(file)
      println("Processing: ${file.name}")
      if (!imgAppender.addImg(img)) {
        imgAppender.writeImage(output)
        imgAppender = new ImageAppender(imgAppender)
        imgAppender.addImg(img)
      }
    }
  }
  imgAppender.writeImage(output)
}

println(args)

if (args.length < 1) {
  println(usage())
  return
}

run(args)

