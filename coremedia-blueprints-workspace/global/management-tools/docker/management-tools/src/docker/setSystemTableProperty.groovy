con = com.coremedia.cap.Cap.connect(null, System.getenv("TOOLS_USER"), System.getenv("TOOLS_PASSWORD"));
cr = con.getContentRepository();
ps = cr.getPropertyService();

ps.put(System.getProperty("propKey"), System.getProperty("propVal"));

con.close();
