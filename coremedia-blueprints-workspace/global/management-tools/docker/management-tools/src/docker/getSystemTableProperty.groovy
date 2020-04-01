con = com.coremedia.cap.Cap.connect(null, System.getenv("TOOLS_USER"), System.getenv("TOOLS_PASSWORD"));
cr = con.getContentRepository();
ps = cr.getPropertyService();

val = ps.get(System.getProperty("propKey"));

System.out.println((val == null) ? "undefined" : val);

con.close();