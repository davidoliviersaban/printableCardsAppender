# Introduction
This project is about aggregating into one or several sheets of paper a lot of cards that you have prepared.

This project came to my mind as I have designed several card games using gimp first then a cool opensource project called Squib (using Ruby :/ ).
I was spending hours appending the generated cards altogether manually using gimp into several A4 sheets to print them and so I got bored and I created this small program.

This simple tool doesn't make any magic. It just automates what I was doing for hours in a few seconds.

# Limitations
Limitations of the tool are linked to images larger than the output paper.
Limitations linked to java's BufferedImage... which should be fine as Java supports most of the images formats.
Limitations in the way cards/images are combined. I could have looked at the best way to put files together and limit paper waste.
For now, I use it to append images that are generated with meaningful names and I prefer to have files with the same name on the same sheet of paper.

# Some commands

To run this program just execute:
```
#If you don't have groovy:
./gradlew run --args="arg1 arg2 arg3 ..."

#If you do have groovy set on your computer:
./src/main/groovy/printableCardAppender.groovy arg1 arg2 arg3 ...
```

Execute the following command to trigger the help:
```
./gradlew appendCards

> Task :appendCards
[]
Usage:
printableCardAppender <input_folder> [output_folder image_format isLandscape ppi margin]OPTIONS:

        intput_folder   (mandatory) folder containing all images that should be merged into a A4 format.
        output_folder   folder & filename prefix for the output A4 images. Default value is ./a4image
        image_format    format of the output image. default format is A4. Most printable formats are supported (see javadoc for javax.print.attribute.standard.MediaSizeName)
        isLandscape     whether the image should be in landscape (true) or portrait(false). Default value is false (portrait)
        ppi             pixels per inch you wish to get. Default value is 300
        margin          number of inches you wish to keep as margin to print your final image. Default value is 0.25 (= 0.25in ~= 6mm)
```

## Example:
Basic call to the program to get A4 printable cards:
```
./gradlew appendCards --args="../myCardGameFolder ../appendedCards/printableA4Cards"
```
# Make Sprite
Make sprite transforms an image into sprites. It uses a mask to remove images parts and make them transparent.
```shell
./gradlew makeSprite --args="../windwalkers-cardgame/_terrain/  ../windwalkers-cardgame/ui_assets/mask.png ../windwalkers-ui/Windwalkers/Assets/Resources/_terrain"
```
