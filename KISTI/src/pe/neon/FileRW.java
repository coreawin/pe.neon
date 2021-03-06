package pe.neon;

import java.io.*;

public abstract class FileRW {

    protected int lineCount = 0;
    protected void readFile(File path) {
        BufferedReader reader = createReader(path, null);
        String line = null;
        lineCount = 0;
        while (true) {
            try {
                line = reader.readLine();
                if (line == null) break;
                lineCount += 1;
                readline(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    protected BufferedReader createReader(File path, String encode) {
        BufferedReader br = null;
        if (encode == null) {
            encode = "UTF-8";
        }
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(path), encode));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return br;
    }

    public void close(Reader r) {
        if (r != null) {
            try {
                r.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close(Writer r) {
        if (r != null) {
            try {
                r.flush();
            } catch (IOException e) {
                //ignore;
            } finally{
                try {
                    r.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 한줄 읽는다.<br>
     *
     * @param line
     */
    public abstract void readline(String line);

    public BufferedWriter createWriter(File path, String encode) {
        BufferedWriter bw = null;
        if (encode == null) {
            encode = "UTF-8";
        }
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), encode));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            close(bw);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            close(bw);
        }
        return bw;
    }

}
