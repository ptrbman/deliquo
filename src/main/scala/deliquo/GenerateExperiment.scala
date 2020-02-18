package deliquo

import java.io.File
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.event.ActionEvent
import scalafx.geometry.{Insets, Orientation}
import scalafx.collections.ObservableBuffer
import scalafx.scene.{Cursor, Scene}
import scalafx.scene.paint.Color
import scalafx.scene.control.TextFormatter.Change
import scalafx.scene.control.{Button, CheckBox, Label, ListCell, ListView, TextArea, TextField, TextFormatter}
import scalafx.scene.layout.{BorderPane, VBox, HBox}
import scalafx.util.StringConverter
import scalafx.scene.layout.VBox
import scalafx.scene.text.Text
import scalafx.scene.control.{RadioButton, ToggleGroup}
import scalafx.stage.FileChooser
import scalafx.stage.DirectoryChooser
import scalafx.stage.FileChooser.ExtensionFilter


object GenerateExperiment extends JFXApp {

  val toolFiles =
    new File("tools/").listFiles.filter(_.isFile).filter(_.getName.endsWith(".tool")).toList
  val toolBoxes = 
    for (tf <- toolFiles) yield {
      new CheckBox { text = Tool.fromXML(tf.getPath).name }
    }

  val nameField = new TextField { text = "experiment_name" }
  val outputField = new TextField { text = "experiment_output.out" }
  val timeoutField = new TextField { text = "60" }
  val expOutputField = new TextField { text = "experiment.exp" }      

  val fileList = new ListView[String] {
    orientation = Orientation.Vertical
    cellFactory = {
      p => {
        val cell = new ListCell[String]
        cell.textFill = Color.Blue
        cell.cursor = Cursor.Hand
        cell.item.onChange { (_, _, str) => cell.text = str }
        cell
      }
    }
    items = ObservableBuffer(List("One", "Two", "three"))
  }

  val selectFolderButton = new Button("Select Folder") {
    onMouseClicked = handle {
      chooseFolder()
    }
  }

  val generateButton = new Button("Generate") {
    prefWidth = 800
    prefHeight = 100
    onMouseClicked = handle {
      generateExperiment()
    }
  }  


  def hd(text_ : String) = {
    new Text {
      text = text_
      style = "-fx-font: normal bold 24pt sans-serif"
    }
  }


  stage = new PrimaryStage {
    scene = new Scene(300, 200) {
      title = "UppSAT"
      root = new VBox {
        spacing = 6
        padding = Insets(10)
        children = new VBox {
          children = Seq(
            hd("Name"), nameField,
            hd("Output"), outputField,
            hd("Timeout"), timeoutField,
            hd("Folder"), fileList, selectFolderButton,
            hd("ToolConfigs")) ++ toolBoxes ++ Seq(
            hd("Experiment File output"), expOutputField,
              generateButton
          )
        }

      }
    }
  }



  def getRecursiveListOfFiles(dir: File): Array[File] = {
    val these = dir.listFiles
    these.filter(_.isFile) ++ these.filter(_.isDirectory).flatMap(getRecursiveListOfFiles)
  }

  def chooseFolder() = {
    val dirChooser = new DirectoryChooser {
      title = "Select Folder"
    }
    val selectedFolder = dirChooser.showDialog(stage)
    if (selectedFolder != null) {
      fileList.items =
        ObservableBuffer(getRecursiveListOfFiles(selectedFolder).map(_.getPath).toList)
      val splitPath = selectedFolder.getPath.split('/')
      outputField.text = splitPath.take(splitPath.length-1).mkString("/") + "/results.out"
      expOutputField.text = splitPath.take(splitPath.length-1).mkString("/") + "/experiment.exp"
    } else {
      Array()
    }
  }


  def generateExperiment() = {
    val inputs  =
      (for (str <- fileList.getItems()) yield {
        str
      }).toList

    val toolConfigs =
      for (cb <- toolBoxes.filter(_.selected.value)) yield {
        ToolConfig(cb.text.value)
      }

    val exp = Experiment(
      nameField.text.get,
      timeoutField.text.get.toInt,
      outputField.text.get,
      inputs, toolConfigs)

    if (inputs.isEmpty || toolConfigs.isEmpty) {
      sys.error("Nonsensical experiment!")
    } else {
      exp.writeXML(expOutputField.text.value)
    }
  }
}
