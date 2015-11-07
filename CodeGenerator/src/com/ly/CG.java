package com.ly;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

public class CG extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2690103207370201799L;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		this.doPost(req, resp);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

		String path = req.getSession().getServletContext().getRealPath("/");
		int num = path.indexOf(".metadata");
		path = path.substring(0, num).replace('/', '\\');

		String p = req.getParameter("p");
		String t = req.getParameter("t");
		Properties pps = new Properties();
		String url = null;
		String user = null;
		String password = null;
		String driver = null;

		Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);
		cfg.setDirectoryForTemplateLoading(new File(path + "CodeGenerator\\src\\template"));
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

		InputStream in = new BufferedInputStream(new FileInputStream(path + "CodeGenerator\\src\\cg.property"));
		pps.load(in);
		url = pps.getProperty("URL");
		user = pps.getProperty("USER");
		password = pps.getProperty("PASSWORD");
		driver = pps.getProperty("DRIVER");

		Map root = new HashMap();
		try {
			PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource();
			pds.setConnectionFactoryClassName(driver);
			pds.setURL(url);
			pds.setUser(user);
			pds.setPassword(password);
			pds.setInitialPoolSize(5);
			Connection conn = pds.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select * from user_tab_columns where table_name='" + t + "'");
			root.put("package", p);
			root.put("class", t);
			Collection<Map<String, String>> columns = new HashSet<Map<String, String>>();
			while (rs.next()) {
				Map<String, String> temp = new HashMap<String, String>();
				temp.put("column", rs.getString(2));
				temp.put("property", rs.getString(2).toLowerCase());
				temp.put("jdbctype", rs.getString(3));
				String s = rs.getString(3);
				if (s.toUpperCase().equals("VARCHAR2")) {
					s = "String";
				} else if (s.toUpperCase().equals("NUMBER")) {
					if (Integer.parseInt(rs.getString(8)) == 0)
						s = "Long";
					else
						s = "BigDecimal";
				} else if (s.toUpperCase().equals("DATE")) {
					s = "String";
				} else {
					try {
						throw new Exception("不支持的类型");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				temp.put("javatype", s);
				columns.add(temp);
			}
			root.put("columns", columns);
			conn.close();
			conn = null;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		File dic = new File(path + "CodeGenerator\\WebContent\\RESULT\\" + t + Long.toString(System.nanoTime()));
		if (!dic.exists() && !dic.isDirectory())
			dic.mkdir();
		else
			dic.delete();

		Writer out = null;
		Template temp = null;
		File file = null;
		List l = new ArrayList<String>();
		String s = null;

		s = dic.getPath() + "\\" + t + ".java";
		l.add(s);
		file = new File(s);
		file.createNewFile();
		out = new OutputStreamWriter(new FileOutputStream(file.getPath()));
		temp = cfg.getTemplate("entity.ftl");
		try {
			temp.process(root, out);
		} catch (TemplateException e) {
			e.printStackTrace();
		}

		s = dic.getPath() + "\\" + t + ".xml";
		l.add(s);
		file = new File(s);
		file.createNewFile();
		out = new OutputStreamWriter(new FileOutputStream(file.getPath()));
		temp = cfg.getTemplate("xml.ftl");
		try {
			temp.process(root, out);
		} catch (TemplateException e) {
			e.printStackTrace();
		}

		s = dic.getPath() + "\\" + t + "Dao.java";
		l.add(s);
		file = new File(s);
		file.createNewFile();
		out = new OutputStreamWriter(new FileOutputStream(file.getPath()));
		temp = cfg.getTemplate("dao.ftl");
		try {
			temp.process(root, out);
		} catch (TemplateException e) {
			e.printStackTrace();
		}

		s = dic.getPath() + "\\" + t + "Service.java";
		l.add(s);
		file = new File(s);
		file.createNewFile();
		out = new OutputStreamWriter(new FileOutputStream(file.getPath()));
		temp = cfg.getTemplate("service.ftl");
		try {
			temp.process(root, out);
		} catch (TemplateException e) {
			e.printStackTrace();
		}

		s = dic.getPath() + "\\" + t + "Action.java";
		l.add(s);
		file = new File(s);
		file.createNewFile();
		out = new OutputStreamWriter(new FileOutputStream(file.getPath()));
		temp = cfg.getTemplate("action.ftl");
		try {
			temp.process(root, out);
		} catch (TemplateException e) {
			e.printStackTrace();
		}
		
		s = dic.getPath() + "\\" + t + ".jsp";
		l.add(s);
		file = new File(s);
		file.createNewFile();
		out = new OutputStreamWriter(new FileOutputStream(file.getPath()));
		temp = cfg.getTemplate("jsp.ftl");
		try {
			temp.process(root, out);
		} catch (TemplateException e) {
			e.printStackTrace();
		}

		out.close();
		out = null;

		for (int i = 0; i < l.size(); i++) {
			System.out.println(l.get(i).toString());
		}

		req.setAttribute("urls", l);
		req.getRequestDispatcher("/result.jsp").forward(req, resp);

	}

}