#!/usr/bin/env groovy

import javax.imageio.ImageIO
import javax.print.attribute.standard.MediaSize
import javax.print.attribute.standard.MediaSizeName
import java.awt.image.BufferedImage

/**
 * This class appends into one image several images added through the method addImg
 * Once the image is full or no more image is to be added, the image can be output as a PNG file
 */
class MakeSprite {

  BufferedImage mask
  String output_format

  /**
   * Adds/Appends to the image inside the current bigger image
   * @param bi smaller image that should be put into a bigger image
   * @return the image can be added or there is not enough empty space to add this image
   */
  boolean process(BufferedImage image, String output) {
    if (!image.getColorModel().hasAlpha()) {
      println "[WARNING] Image doesn't have alpha channel -> image skipped"
      return false;
    }
    final int width = image.getWidth()
    int[] imgData = new int[width]
    int[] maskData = new int[width]

    for (int y = 0; y < image.getHeight(); y++) {
      // fetch a line of data from each image
      image.getRGB(0, y, width, 1, imgData, 0, 1)
      mask.getRGB(0, y, width, 1, maskData, 0, 1)
      // apply the mask
      for (int x = 0; x < width; x++) {
        int color = imgData[x] & 0x00FFFFFF // mask away any alpha present
        int maskColor = (maskData[x] & 0x00FF0000) << 8 // shift red into alpha bits
        color |= maskColor
        imgData[x] = color
      }
      // replace the data
      image.setRGB(0, y, width, 1, imgData, 0, 1)
    }
    ImageIO.write(image, output_format.toUpperCase(), new File("${output}.sprite.${output_format}"))
  }

}

def usage() {
  "Usage:\n"+
  "printableCardAppender <input_folder> [output_folder image_format isLandscape ppi margin]" +
  "OPTIONS:\n"+
    "\n\tintput_folder\t(mandatory) folder containing all images that should be merged into a A4 format." +
    "\n\tmask         \t(mandatory) path to the mask to use to transform image into sprite"
    "\n\toutput_folder\t(optional) folder that will contain all output sprites"
}

def run(args) {
  def input = args[0]
  def mask  = args[1]
  def output= args.length>2?args[2]:input

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

  MakeSprite makeSprite = new MakeSprite(output_format: "png", mask: ImageIO.read(new File(mask)))

  path.listFiles().sort().each({ file ->
    if (file.isFile() && file.name.endsWith("png")) { //this line weeds out other directories/foldersB
      BufferedImage img = ImageIO.read(file)
      println("Processing: ${file.name}")
      makeSprite.process(img,output+"/"+file.name)
    }
  })
}

println(args)

if (args.length < 1) {
  println(usage())
  return
} else {
  run(args)
}
