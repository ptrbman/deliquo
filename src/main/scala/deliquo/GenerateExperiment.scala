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
  // var configurations : List[(String, List[(String, String)], String)] = List()
  var configurations : List[ToolConfig] = List()

  
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
  val timeoutField = new TextField { text = "60" }


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

  val loadExperimentButton = new Button("Load Experiment") {
    onMouseClicked = handle {
      loadExperiment()
    }
  }


  val generateButton = new Button("Save Experiment") {
    prefWidth = 800
    prefHeight = 100
    onMouseClicked = handle {
      generateExperiment()
    }
  }

  val deleteConfigButton = new Button("Delete Configuration") {
    onMouseClicked = handle{
      deleteConfig()
    }
  }


  val toolPane = new TabPane
    val tabs : List[Tab] = 
      for (t <- tools) yield {
        val text = t.name
        val tab = new Tab
        val tabData = 
          for ((option, values) <- t.options) yield {
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

        val extraField = new TextField { text = "" }

        val addButton = new Button("Add Config") {
          onMouseClicked = handle {
            val tc = ToolConfig(t.name, tabData.map(_._2()).toMap, extraField.text.get)
            addConfig(tc)
          }
        }

        val layout = new HBox { children = addButton :: extraField :: tabData.map(_._1) }
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
            loadExperimentButton,
            (new HBox { children = Seq(hd("Name"), nameField) }),
            (new HBox { children = Seq(hd("Timeout"), timeoutField) } ),
            hd("Folder"), fileList, selectFolderButton,
            hd("ToolConfigs"), toolPane, configList,deleteConfigButton,
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

  def addFiles(files : List[String]) = {
    fileList.items = ObservableBuffer(files)
  }

  def chooseFolder() = {
    val dirChooser = new DirectoryChooser {
      title = "Select Folder"
      initialDirectory = new File("/home/ptr/experiments/")
    }

    val selectedFolder = dirChooser.showDialog(stage)
    if (selectedFolder != null) {
      addFiles(getRecursiveListOfFiles(selectedFolder).map(_.getPath).toList)
    }
  }

  def loadExperiment() = {
    val fileChooser = new FileChooser {
      title = "Load Experiment"
      initialDirectory = new File("/home/ptr/experiments/")
      extensionFilters ++= Seq(new ExtensionFilter("Experiment Files", "*.exp"))
    }
    val selectedFile = fileChooser.showOpenDialog(stage)

    val exp = Experiment(selectedFile.getAbsolutePath())
    nameField.text.set(exp.name)
    timeoutField.text.set(exp.timeout.toString)
    addFiles(exp.inputs)
    configurations = List()
    for (config <- exp.toolConfigs) {
      addConfig(config)
    }
  }

  def updateConfigList() = {
    val listStrings =
      for ((conf,i) <- configurations.zipWithIndex) yield {
        val name = conf.toolName
        val options = conf.optionValues.map{ case (o,v) => o + ":" + v}.mkString("|")
        val extra = conf.extras
        i + "###" + name + " " + options + " " + extra
      }
    configList.items = ObservableBuffer(listStrings)
  }


  def addConfig(config : ToolConfig) = {
    configurations =  config :: configurations
    updateConfigList()
  }

  def deleteConfig() = {
    val configId = configList.getSelectionModel().getSelectedItem().split("###")(0).toInt
    configurations = configurations.take(configId) ++ configurations.drop(configId + 1)
    updateConfigList()
  }

  def generateExperiment() = {
    val inputs  =
      (for (str <- fileList.getItems()) yield {
        str
      }).toList


    val toolConfigs = configurations

    if (inputs.isEmpty || toolConfigs.isEmpty) {
      sys.error("Nonsensical experiment!")
    } else {


      val fileChooser = new FileChooser {
        title = "Save Experiment"
        initialDirectory = new File("/home/ptr/experiments/")
        extensionFilters ++= Seq(new ExtensionFilter("Experiment Files", "*.exp"))
      }

      val expFile = fileChooser.showSaveDialog(stage)


    val splitPath = expFile.getAbsolutePath().split('/')
    val outFile = splitPath.take(splitPath.length-1).mkString("/") + "/" + nameField.text.get + ".out"



      val exp = Experiment(
        nameField.text.get,
        timeoutField.text.get.toInt,
        outFile,
        inputs, toolConfigs)

      if (exp != null) {
        exp.writeXML(expFile.getAbsolutePath())
      }
    }
  }
}
