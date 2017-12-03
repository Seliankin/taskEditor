package seliankin;

import com.sun.org.apache.xml.internal.serializer.OutputPropertiesFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ListTasks {
    public static void main(String[] args)
    {
        System.out.println("Запущено приложение \"Менеджер задач\"");
        TaskManager taskManager = new TaskManager("tasks.xml");
        Scanner scanner = new Scanner(System.in);
        while(true) {
            String command = scanner.nextLine();
            if(!taskManager.parseCommand(command)) {
                break;
            }
        }
        scanner.close();
    }
}

class TaskManager
{
    private String filePath;
    static int counter = 0;
    ArrayList<String> filterNodes;
    SimpleDateFormat dataFormat;
    DocumentBuilder documentBuilder;
    Document document;

    TaskManager(String filePath)
    {
        dataFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.filePath = filePath;
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = documentBuilder.parse(filePath);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        filterNodes = new ArrayList<String>();
        filterNodes.add("Description");
        filterNodes.add("Priority");
        filterNodes.add("Deadline");
    }

    public boolean parseCommand(String t_command)
    {
        String[] partCommand = t_command.split(" ");
        String command = partCommand[0];
        String paramCommand = "";
        for(int i = 1; i < partCommand.length; i++){
            if(!partCommand[i].isEmpty()){
                paramCommand = partCommand[i];
                break;
            }
        }
        switch(command.toLowerCase()) {
            case "help": {//+
                printHelp();
                break;
            }
            case "new": {//+
                appendTask();
                break;
            }
            case "list": {//+
                if(paramCommand.isEmpty())
                    printTasks("");
                else if(paramCommand.equals("-s")) {
                    if(partCommand.length > 2) {
                        printTasks(partCommand[2].toLowerCase());
                    }
                }
                else
                    System.out.println("Неизвестный параметр " + paramCommand);
                break;
            }
            case "complete": {//+
                try {
                    int id = Integer.parseInt(paramCommand);
                    setComplete(id);
                }
                catch (NumberFormatException e) {
                    System.out.println("Некорректный параметр id");
                }
                break;
            }
            case "edit": {//+
                try {
                    int id = Integer.parseInt(paramCommand);
                    editTask(id);
                }
                catch (NumberFormatException e) {
                    System.out.println("Некорректный параметр id");
                }
                break;
            }
            case "remove": {//+
                try {
                    int id = Integer.parseInt(paramCommand);
                    removeTask(id);
                }
                catch (NumberFormatException e) {
                    System.out.println("Некорректный параметр id");
                }
                break;
            }
            case "exit": {//+
                System.out.println("Завершение работы программы");
                return false;
            }
            default: {
                System.out.println("Комманда " + command + " не определена");
                break;
            }
        }
        return true;
    }

    public void printTasks(String status)
    {
        Node root = document.getDocumentElement();
        NodeList tasks = root.getChildNodes();
        for (int i = 0; i < tasks.getLength(); i++) {
            Node task = tasks.item(i);
            if (task.getNodeType() == Node.TEXT_NODE) continue;
            NodeList childs = task.getChildNodes();
            boolean isStatus = true;
            for (int k = 0; k < childs.getLength(); k++) {
                Node child = childs.item(k);
                if (child.getNodeType() == Node.TEXT_NODE) continue;
                if(child.getNodeName().equals("Status")){
                    if(!status.equals(child.getTextContent())){
                        isStatus = false;
                        break;
                    }
                }
            }
            if(!status.isEmpty() && !isStatus) continue;
            System.out.print(task.getNodeName() + " ");
            NamedNodeMap attributes = task.getAttributes();
            for (int j = 0; j < attributes.getLength(); j++) {
                Node atrtribute = attributes.item(j);
                System.out.print(atrtribute.getNodeName() + " " + atrtribute.getNodeValue() + " ");
            }
            System.out.println();

            for (int k = 0; k < childs.getLength(); k++) {
                Node child = childs.item(k);
                if (child.getNodeType() == Node.TEXT_NODE) continue;
                System.out.println("\t" + child.getNodeName() + " " + child.getTextContent());
            }
            System.out.println("===========>>>>");
        }
    }

    public void appendTask()
    {
        Scanner scanner = new Scanner(System.in);
        int intValue = 0;
        String stringValue = "";

        Element root = document.createElement("Task");
        System.out.println("Введите id задачи: ");
        intValue = getValidateInt(scanner.nextLine());
        System.out.println("Введите заголовок задачи: ");
        stringValue = scanner.nextLine();
        root.setAttribute("id", Integer.toString(intValue));
        root.setAttribute("caption", stringValue);

        System.out.println("<Description>");
        //if(scanner.hasNext())
            stringValue = scanner.nextLine();
        Node item = document.createElement("Description");
        item.appendChild(document.createTextNode(stringValue));
        root.appendChild(item);

        System.out.println("<Priority>");
        intValue = getValidateInt(scanner.nextLine());
        item = document.createElement("Priority");
        item.appendChild(document.createTextNode(Integer.toString(intValue)));
        root.appendChild(item);

        System.out.println("<Deadline> (yyyy-MM-dd))");
        stringValue = getValidateDate(scanner.nextLine());
        item = document.createElement("Deadline");
        item.appendChild(document.createTextNode(stringValue));
        root.appendChild(item);

        item = document.createElement("Status");
        item.appendChild(document.createTextNode("new"));
        root.appendChild(item);

        System.out.println("<Complete> (yyyy-MM-dd))");
        stringValue = getValidateDate(scanner.nextLine());
        item = document.createElement("Complete");
        item.appendChild(document.createTextNode(stringValue));
        root.appendChild(item);

        System.out.println("Задача успешно добавлена");
        document.getDocumentElement().appendChild(root);
        writeDocument(document);
    }

    private int getValidateInt(String value)
    {
        int intValue = 0;
        Scanner scanner = new Scanner(System.in);
        while(true) {
            try {
                intValue = Integer.parseInt(value);
                return intValue;
            }
            catch (NumberFormatException e) {
                System.out.println("Некорректная входная информация");
                value = scanner.nextLine();
            }
        }
    }

    private String getValidateDate(String value)
    {
        String stringValue = "";
        Scanner scanner = new Scanner(System.in);
        while(true) {
            stringValue = value;
            try {
                Date date = dataFormat.parse(stringValue);
                if (!dataFormat.format(date).equals(stringValue)) {
                    System.out.println("Некорректный формат даты1");
                }
                else {
                    return stringValue;
                }
            } catch (ParseException e) {
                System.out.println("Некорректный формат даты");
                value = scanner.nextLine();
            }
        }
    }

    public void removeTask(int id)
    {
        Element root = document.getDocumentElement();
        boolean isRemoved = false;
        while(true) {
            Node item_task = findNode(id);
            if(item_task == null) break;
            root.removeChild(item_task);
            isRemoved = true;
        }
        if(isRemoved) {
            System.out.println("Задача с идентификатором " + Integer.toString(id) + " успешно удалена");
            writeDocument(document);
        }
        else
            System.out.println("Задача с id = " + Integer.toString(id) + " не найдена");
    }

    public void editTask(int id)
    {
        Node item_task = findNode(id);
        if(item_task == null) {
            System.out.println("Задача с id = " + Integer.toString(id) + " не найдена");
            return;
        }
        System.out.println("Если хотите оставить поле без изменений, нажмите \"Enter\"");

        Scanner scanner = new Scanner(System.in);
        NamedNodeMap attributes = item_task.getAttributes();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                if (attributes.item(i).getNodeName().equals("caption")) {
                    System.out.println(attributes.item(i).getNodeName());
                    String valueCaption = scanner.nextLine();
                    if (!valueCaption.isEmpty())
                        attributes.item(i).setNodeValue(valueCaption);
                }
            }
        }
        NodeList taskParams = item_task.getChildNodes();
        for(int k=0; k < taskParams.getLength(); k++){
            if (taskParams.item(k).getNodeType() == Node.TEXT_NODE) continue;
            String nodeName = taskParams.item(k).getNodeName();
            if(!filterNodes.contains(nodeName)) continue;
            System.out.println(nodeName);

            String value = scanner.nextLine();
            if(value.isEmpty()) continue;
            switch (nodeName) {
                case "Description":
                    break;
                case "Priority":
                    value = Integer.toString(getValidateInt(value));
                    break;
                case "Deadline":
                    value = getValidateDate(value);
                    break;
            }
            if(taskParams.item(k).hasChildNodes()) {
                taskParams.item(k).getFirstChild().setNodeValue(value);
            }
        }

        System.out.println("Задача с идентификатором " + Integer.toString(id) + " успешно изменена");
        writeDocument(document);
    }

    public void setComplete(int id)
    {
        Node item_task = findNode(id);
        if(item_task == null) {
            System.out.println("Задача с id = " + Integer.toString(id) + " не найдена");
            return;
        }
        NodeList taskParams = item_task.getChildNodes();
        for(int k=0; k < taskParams.getLength(); k++){
            if (taskParams.item(k).getNodeType() == Node.TEXT_NODE) continue;
            String s = taskParams.item(k).getNodeName();
            if(s.equals("Complete")){
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = new Date();
                taskParams.item(k).getFirstChild().setNodeValue(dateFormat.format(date));
            }
        }

        System.out.println("Задача с идентификатором " + Integer.toString(id) + " успешно изменена");
        writeDocument(document);
    }

    private Node findNode(int id)
    {
        Element root = document.getDocumentElement();
        NodeList listTasks = root.getChildNodes();
        int count  = listTasks.getLength();
        for(int i = 0; i<listTasks.getLength(); i++) {
            NamedNodeMap attributes = listTasks.item(i).getAttributes();
            if (attributes == null) continue;
            for (int j = 0; j < attributes.getLength(); j++) {
                if (attributes.item(j).getNodeName().equals("id")) {
                    if (attributes.item(j).getNodeValue().equals(Integer.toString(id))) {
                        return listTasks.item(i);
                    }
                }
            }
        }
        return null;
    }

    private void writeDocument(Document document) throws TransformerFactoryConfigurationError
    {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT,"yes");
            transformer.setOutputProperty(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT, "4");
            DOMSource source = new DOMSource(document);
            FileOutputStream fos = new FileOutputStream(filePath);
            StreamResult result = new StreamResult(fos);
            transformer.transform(source, result);
        } catch (TransformerException | IOException e) {
            e.printStackTrace(System.out);
        }
    }

    public void printHelp()
    {
        System.out.println("new                 создание новой задачи");
        System.out.println("list        -s      \"new\", \"done\", \"in_progress\" вывод задач по статусу выполнения");
        System.out.println("                    без параметра - вывод всех задач");
        System.out.println("complete    id      пометить задачу как выполненную по номеру id");
        System.out.println("edit        id      редактирование задачи по номеру id");
        System.out.println("remove      id      удаление задачи по номеру id");
        System.out.println("exit                завершить работу приложения");
    }
}
