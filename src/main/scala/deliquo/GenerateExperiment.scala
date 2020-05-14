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
import scalafx.scene.control.{Button, CheckBox, Label, ListCell, ListView, TextArea, TextField, TextFormatter, Tab, TabPane, RadioButton, ToggleGroup}
import scalafx.scene.layout.{BorderPane, VBox, HBox}
import scalafx.util.StringConverter
import scalafx.scene.text.Text
import scalafx.stage.{FileChooser, DirectoryChooser}
import scalafx.stage.FileChooser.ExtensionFilter


object GenerateExperiment extends JFXApp {

  // Storing the selected Configurations
  // Configuration = (ToolName, (Option, Value)*, Extra)
  var configurations : List[(String, List[(String, String)], String)] = List()

  
  // Load tools
  val toolFiles =
    new File("tools/").listFiles.filter(_.isFile).filter(_.getName.endsWith(".tool")).toList
  val tools = for (tf <- toolFiles) yield Tool.fromXML(tf.getPath)


  // ------------------------------
  // Create all graphical components
  // ------------------------------

  val toolBoxes : Map[Tool, CheckBox] =
    (for (t <- tools) yield {
      t -> new CheckBox { text = t.name }
    }).toMap

  // TextFields
  val nameField = new TextField { text = "experiment_name" }
  val outputField = new TextField { text = "experiment_output.out" }
  val timeoutField = new TextField { text = "60" }
  val expOutputField = new TextField { text = "experiment.exp" }


  // List of files
  val fileList = new ListView[String] {
    orientation = Orientation.Vertical
    prefHeight = 100
    cellFactory = {
      p => {
        val cell = new ListCell[String]
        cell.textFill = Color.Blue
        cell.cursor = Cursor.Hand
        cell.item.onChange { (_, _, str) => cell.text = str }
        cell
      }
    }
    items = ObservableBuffer(List("..."))
  }

  // List of Configurations
  val configList = new ListView[String] {
    orientation = Orientation.Vertical
    prefHeight = 100
    cellFactory = {
      p => {
        val cell = new ListCell[String]
       cell.textFill = Color.Blue
        cell.cursor = Cursor.Hand
        cell.item.onChange { (_, _, str) => cell.text = str }
        cell
      }
    }
    items = ObservableBuffer(List(""))
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


  val toolPane = new TabPane
    val tabs : List[Tab] = 
      for (t <- tools) yield {
        val text = t.name
        val tab = new Tab
        val tabData = 
          for ((option, values) <- t.options) yield {
            println(option + "->" + values)
            val tg = new ToggleGroup
            val buttons =
              for ((value, argument) <- values) yield {
                val rb = new RadioButton(value)
                rb.setToggleGroup(tg)
                rb
              }
            val tabLayout = new VBox { children = buttons }
            val tabFunction = () => {
              (option, buttons.filter(_.isSelected()).head.text.value)
            }
            (tabLayout, tabFunction)
          }

        tab.text = text
        tab.setClosable(false)
        val addButton = new Button("Add Config") {
          onMouseClicked = handle {
            println(t.name)
            addConfig(t.name, tabData.map(_._2()), "-v")
          }
        }
        val layout = new HBox { children = addButton :: tabData.map(_._1) }
        tab.content = layout
        tab
      }

  toolPane.tabs = tabs.toList


  def hd(text_ : String) = {
    new Text {
      text = text_
      style = "-fx-font: normal bold 12pt sans-serif"
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
            (new HBox { children = Seq(hd("Name"), nameField) }),
            (new HBox { children = Seq(hd("Output"), outputField) } ),
            (new HBox { children = Seq(hd("Timeout"), timeoutField) } ),
            hd("Folder"), fileList, selectFolderButton,
            hd("ToolConfigs"), toolPane, configList,
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


  def addConfig(config: (String, List[(String, String)], String)) = {
    configurations =  config :: configurations
    val listStrings = 
      for (conf <- configurations) yield {
        val (toolName, optionValues, extras) = conf
        toolName + " " + optionValues.map{ case (o,v) => o + ":" + v}.mkString("|") + " " + extras
      }
    configList.items = ObservableBuffer(listStrings)
  }

  def generateExperiment() = {
    val inputs  =
      (for (str <- fileList.getItems()) yield {
        str
      }).toList

    val toolConfigs =
      (for (cb <- toolBoxes.map(_._2).filter(_.selected.value)) yield {
        ToolConfig(cb.text.value)
      }).toList

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
