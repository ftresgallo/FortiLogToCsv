import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class FortiLogToCsv {
    public static void main(String[] args) {

        if (args == null || args.length != 2) {
            System.out.println("Specify arguments as: <inFilename> <outFilename>");
            System.exit(1);
        }
        String inFilename = args[0];
        String outFilename = args[1];

        BufferedReader reader;
        BufferedWriter writer;

        Pattern pattern1 = Pattern.compile("(\\S+)=(\"[^\"]+\")\\s*(.*)");
        Pattern pattern2 = Pattern.compile("(\\S+)=(\\S+)\\s*(.*)");

        String line;
        String header, data, rest = null;

        Set<String> headers = new LinkedHashSet<String>();  

        try {

            // Build headers

            reader = new BufferedReader(new FileReader(inFilename));

            line = reader.readLine();

            while (line != null) {

                String fragment = line;
    
                do {
                    Matcher matcher1 = pattern1.matcher(fragment);
                    Matcher matcher2 = pattern2.matcher(fragment);
                    
                    if (matcher1.matches()) {
                        header = matcher1.group(1);
                        headers.add(header);
                        rest =  matcher1.group(3);
                    } else if (matcher2.matches()) {
                        header = matcher2.group(1);
                        headers.add(header);
                        rest =  matcher2.group(3);
                    } else {
                        System.out.println("Unexpected input, exiting");
                        System.out.println("fragment: \"" + fragment + "\"");
                        System.exit(1);
                    } 
                    fragment = rest;
                } while (fragment != null && !fragment.isEmpty()); 
    
                line = reader.readLine();
            } 

            reader.close();

            // Write headers in CSV

            int i = 0;
            int len = headers.size();
            writer = new BufferedWriter(new FileWriter(outFilename));
            for (String item : headers) {
                i++;
                if (len <= i) {
                    writer.write(item);
                } else {
                    writer.write(item + ",");
                }   
            }

            // Build and write values

			reader = new BufferedReader(new FileReader(inFilename));

            line = reader.readLine();
			
            while (line != null) {

                Hashtable<String, String> ht = new Hashtable<>();

                String fragment = line;

                do {
                    Matcher matcher1 = pattern1.matcher(fragment);
                    Matcher matcher2 = pattern2.matcher(fragment);
                    
                    if (matcher1.matches()) {
                        header = matcher1.group(1);
                        data = matcher1.group(2);

                        ht.put(header, data);

                        rest =  matcher1.group(3);

                    } else if (matcher2.matches()) {
                        header = matcher2.group(1);
                        data = matcher2.group(2);

                        ht.put(header, data);

                        rest =  matcher2.group(3);

                    } else {
                        System.out.println("Unexpected input, exiting");
                        System.out.println("fragment: \"" + fragment + "\"");
                        System.exit(1);
                    } 
                    fragment = rest;
                } while (fragment != null && !fragment.isEmpty()); 

                writer.write("\n");
                i = 0;
                len = headers.size();
                for (String key : headers) {
                    i++;
                    String value = ht.get(key);
                    if (value != null) {
                        if (len <= i) {
                            writer.write(value);
                        } else {
                            writer.write(value + ",");
                        }
                    } else {
                        if (len <= i) {
                            writer.write("");
                        } else {
                            writer.write(",");
                        }
                    }
                }

                line = reader.readLine();
            }      

			reader.close();

            writer.close();
        } catch (IOException e) {
			e.printStackTrace();
            System.exit(1);
        }
    }
}

