import java.io.*;
import java.util.*;


public class Alecqs {

   public static final String PROPSNAME = "PROPSNAME";
   public static final int NONE  = 0;
   public static final int ERROR = 10;
   public static final int WARN  = 20;
   public static final int INFO  = 30;
   public static final int DEBUG = 40;

   public static void main(String args[]) throws IOException    {
      Alecqs parser=new Alecqs();

      File dir=new File(".");
      Map<String,String> commandLineProps = parser.pushVarMap("command-line");
      for (String arg: args) {
         if (arg.indexOf('=')>0)
            parser.parseProp(arg);
         else if (arg.equals("-v"))
            parser.loglevel=INFO;
         else if (arg.equals("-d"))
            parser.loglevel=DEBUG;
         else if (arg.equals("-q"))
            parser.loglevel=NONE;
         else
            parser.parseFile(dir, arg);
      }
      parser.closeOutput();
   }

   public Alecqs() throws FileNotFoundException {
      addProp("dollar","$");
   }

   private String currentFilename=null;
   private PrintStream out = null;
   private ArrayDeque<Map<String,String>> vars = new ArrayDeque<Map<String,String>>();
   public int loglevel=WARN;
   private Map<String,String> globalVars=pushVarMap("global");


   public void popVarMap() { vars.removeFirst(); }
   public void pushVarMap(Map<String,String> map, String name) { map.put(PROPSNAME,name); vars.addFirst(map); }
   public Map<String,String> pushVarMap(String name) { Map<String,String> map=new HashMap<String,String>(); pushVarMap(map,name); return map; }

   public void parseFile(File dir, String filename) throws IOException {
      filename=filename.trim().replace('\\','/');
      log(INFO, "Parsing "+dir.toString().replace('\\','/')+"/"+filename);
      pushVarMap("File:"+filename);

      File file=new File(dir, filename);
      int pos0=filename.lastIndexOf('.');
      String filebase=filename.substring(0,pos0);
      addProp("FILENAME", filename);
      addProp("FILEBASE", filebase);

      BufferedReader inp = null;
      int linenr=0;
      boolean done=false;
      try {
         inp = new BufferedReader(new FileReader(file));
         String line;
         while ((line = inp.readLine()) != null) {
            linenr++;
            linenr+=parseLine(inp, file.getParentFile(), line);
         }
         done=true;
      }
      finally {
         if (! done)
            System.err.println("ERROR at "+filename+":"+linenr);
         if (inp != null)
            inp.close();
      }
   }

   public void loadDefinitions(File dir, String filename) throws IOException { parseFile(dir, filename);}
   //public void includeFile(File dir, String filename) throws IOException { parseFile(dir, filename, true); popVarMap();}

   private int  parseLine(BufferedReader inp, File dir, String line) throws IOException {
      //log(DEBUG, "Reading: "+line);
      if (line.startsWith("@VAR"))
         parseProp(substitute(line.substring(4)));
      else if (line.startsWith("@GLOBAL"))
         parseProp(globalVars, substitute(line.substring(7)));
      //else if (line.startsWith("@INCLUDE"))
      //   includeFile(dir, line.substring(8));
      else if (line.startsWith("@MACRO")) {
         //if (inp==null)
         //   throw new RuntimeException("Can not use @MACRO on command line");
         String name=line.substring(6).trim();
         StringBuilder macro=new StringBuilder();
         int linenr=0;
         while ((line = inp.readLine()) != null) {
            linenr++;
            if (line.trim().startsWith("@ENDMACRO"))
               break;
            else
               macro.append(line+"\n");
         }
         addProp(name,macro.toString());
         return linenr;
      }
      else if (line.startsWith("@RUN")) {
            line=line.substring(4);
            int pos1=line.indexOf('(');
            int pos2=line.indexOf(')');
            if (pos1<=0 || pos2<=0)
                throw new  RuntimeException("@RUN line should contain ( and )");
            String cmd=line.substring(0,pos1).trim();
            log(DEBUG, "Running macro: "+cmd);
            String[] args=line.substring(pos1+1,pos2).split("[,]+");
            Map<String,String> macrovars = pushVarMap("macrovars");
            for (String arg: args) {
               arg=arg.trim();
               if (arg.length()>0)
               parseProp(substitute(arg));
            }
            line=getPropertyValue(cmd);
            for (String l: line.split("\\r\\n|\\n|\\r"))
               parseLine(inp, dir, l);
         popVarMap();
      }
      else if (line.startsWith("@OUTPUTFILE"))
         changeOutputFile(dir, substitute(line.substring(11).trim()));
      else if (line.startsWith("@LOAD"))
         loadDefinitions(dir, line.substring(5));
      else if (line.indexOf('=')>0 && out==null) {
         line=substitute(line);
         parseProp(line.trim());
      }
      else if (out!=null)
         out.println(substitute(line));
      return 0;
   }

   private void log(int level, String s) {
      if (level<=loglevel)
         System.out.println(s);
   }

   private void changeOutputFile(File dir, String filename) throws FileNotFoundException {
      log(INFO, "Setting output to "+filename);

      int pos=filename.lastIndexOf('/');
      if (pos>0)
         filename=filename.substring(pos+1);
      int pos0=filename.lastIndexOf('.');
      if (pos>0)
         filename.substring(0,pos0);

      if (!filename.equals(currentFilename)) {
            if (out!=null)
                out.close();
            out=new PrintStream(new FileOutputStream(new File(dir, filename)));
            currentFilename=filename;
         log(INFO, "Setting output to "+dir.toString().replace('\\','/')+"/"+currentFilename);
        }
   }
   public void closeOutput() {
      if (out != null)
         out.close();
   }

   public String substitute(String str) {
      if (str==null) return null;
      StringBuilder result = new StringBuilder();
      int prevpos=0;
      int pos=str.indexOf("${");
      while (pos>=0) {
         int pos2=str.indexOf("}", pos);
         if (pos2<0)
            throw new RuntimeException("Unbounded ${ starting with "+str.substring(pos,pos+10));
         String key=str.substring(pos+2,pos2);
         result.append(str.substring(prevpos,pos));
         String value=getPropertyValue(key);
         if (value==null && key.equals("dollar"))
            value="$";
         if (value==null)
            throw new RuntimeException("Unknown variable ${"+key+"}");
         result.append(value.toString());
         prevpos=pos2+1;
         pos=str.indexOf("${",prevpos);
      }
      result.append(str.substring(prevpos));
      return result.toString();
   }

   private String getPropertyValue(String key) {
      for (Map<String,String> map: vars) {
         String result =  map.get(key);
         if (result != null)
            return result;
      }
      return null;
   }
   private void parseProp(String arg) { parseProp(vars.peekFirst(), arg);}
   private void parseProp(Map<String,String> map, String arg) {
      int pos= arg.indexOf("=");
      if (pos>0)
         addProp(map, arg.substring(0,pos).trim(), arg.substring(pos+1).trim());
      else
         System.out.println("ignoring argument "+ arg);
   }
   private void addProp(String key, String value) { addProp(vars.peekFirst(),key, value); }
   private void addProp(Map<String,String> map, String key, String value) {
      key=key.trim();
      log(DEBUG, "adding "+key+"="+value+" to "+map.get(PROPSNAME));
      map.put(key,value);
   }
}
 
