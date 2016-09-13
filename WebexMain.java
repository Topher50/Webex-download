package webex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebexMain {
	public static String admin;
	public static String portal;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("start");
		
		System.setProperty("webdriver.chrome.driver", "chromedriver path goes here");//TODO Specify path to chromedriver
		
		WebDriver driver=new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(60,TimeUnit.SECONDS);
		
		String csvfile = System.getProperty("user.home") + "csv path here";//TODO path to csv
		String line = "";
		String name = "";
		String email = "";
		
		//hardcode Admin email, name, and password
		String AdminEmail = ""; //TODO
		String AdminName = ""; //TODO last name
		String pass = ""; //TODO
		//hardcode path where you want files to go
		String destPath = "/Volumes/Untitled/TAKETWO/";
		
		try {
		BufferedReader br = new BufferedReader(new FileReader(csvfile));
		PrintWriter failures = new PrintWriter("failures.txt", "UTF-8");
		PrintWriter winners = new PrintWriter("winners.txt", "UTF-8");
		login(driver, AdminEmail, pass);
		
		while( (line = br.readLine()) != null){
			
			String [] info = line.split(",");
			if (info.length >= 4 && info[3].equals("x")) continue;
			name = info[1];
			email = info[2];
			try{
		
			reassign(driver,email,AdminEmail,name);
		
			dothedownload(driver);
			
			movedata(email, destPath);
		
			reassign(driver,AdminEmail, email, AdminName);
			winners.println(email);
			System.out.println(email);
			}catch(Exception e){
				e.printStackTrace();
				System.out.println(name);
				failures.println(email);
				try{
					File source = new File(System.getProperty("user.home") + "/Downloads");
					FileUtils.cleanDirectory(source);
					reassign(driver,AdminEmail, email,AdminName);
				}catch(Exception e1){
					continue;
				}
				continue;
			}
		
	}
	
	failures.close();
	winners.close();
	if(br != null) br.close();
	} catch (FileNotFoundException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (Exception e) {
		e.printStackTrace();
	}
		System.out.println("Done");
	}



	private static void movedata(String email, String destPath) throws IOException {
		// TODO Auto-generated method stub
		File source = new File(System.getProperty("user.home") + "/Downloads");
		File dest = new File(destPath + email);
		dest.mkdir();
		FileUtils.copyDirectory(source, dest);
		FileUtils.cleanDirectory(source);
	}



	private static void dothedownload(WebDriver driver) throws InterruptedException {
		// TODO Auto-generated method stub
		
		WebDriverWait wait = new WebDriverWait(driver, 60);
		
		driver.switchTo().frame("header");
		driver.findElement(By.id("wcc-lnk-MC")).click();
		driver.switchTo().frame("mainFrame");
		driver.switchTo().frame("menu");
		driver.switchTo().frame("treemenu");
		driver.findElement(By.xpath("//a[@id='wcc-lnk-nbrServiceRecording']/span[2]")).click();
		
		do{
			driver.switchTo().defaultContent();
			driver.switchTo().frame("mainFrame");
			driver.switchTo().frame("main");		
			List<WebElement> mrbtns = driver.findElements(By.xpath("//a[contains(@id,'svc-lnk-more')]"));
			List<WebElement> dbtns = driver.findElements(By.xpath("//a[contains(@id,'svc-lnk-download')]"));
		
		
			for(WebElement mrbtn : mrbtns){
				((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", mrbtn);
				mrbtn.click();
				wait.until(ExpectedConditions.visibilityOf(dbtns.get(mrbtns.indexOf(mrbtn))));
				dbtns.get(mrbtns.indexOf(mrbtn)).click();
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e){} 
			}
		
		}while(nextpage(driver));
		do{
				Thread.sleep(30000);
		}while(dldnotdone());
		
	}



	private static void reassign(WebDriver driver, String from, String to, String name) throws InterruptedException {
		driver.switchTo().window(admin);
		driver.findElement(By.id("wcc-lnk-edituser")).click();
		driver.switchTo().frame("main");
		driver.findElement(By.name("searchEmail")).sendKeys(from);;
		driver.findElement(By.name("searchUser")).click();
		List<WebElement> links = driver.findElements(By.tagName("a"));
 
		for (int i = 1; i<links.size(); i=i+1)
		{
			if(links.get(i).getText().contains(name)){
				links.get(i).click();
				break;
			}
		}
		
		driver.findElement(By.cssSelector("input.btn.btn-success")).click();
		Thread.sleep(5000);
		driver.switchTo().window("ReassignRecordings");
		driver.findElement(By.name("searchKey")).sendKeys(to);
		driver.findElement(By.name("search")).click();
		driver.findElement(By.name("user")).click();
		driver.findElement(By.cssSelector("input.btn.btn-success")).click();
		Thread.sleep(5000);
		driver.switchTo().window("OptionsWin");
		driver.findElement(By.name("button1")).click(); 
		driver.switchTo().window(portal);
	}



	private static boolean nextpage(WebDriver driver) {
		List<WebElement> nxts = driver.findElements(By.xpath("//a[contains(text(),'›')]"));
		if(nxts.isEmpty()){
			return false;
		}
		nxts.get(0).click();
		return true;
	}



	private static boolean dldnotdone() {
		File folder = new File(System.getProperty("user.home") + "/Downloads");
		File[] listfiles = folder.listFiles();
		
		for (int i=0; i<listfiles.length; i++){
			if (listfiles[i].getName().contains(".arf.crdownload")){
				return true;
			}
		}
		return false;
	}



	private static void login(WebDriver driver, String AdminEmail, String pass){
		
		WebDriverWait wait = new WebDriverWait(driver, 60);
		
		driver.get("webex url"); //TODO webex url
		driver.switchTo().frame("header");
		driver.findElement(By.id("wcc-lnk-MW")).click();
		driver.switchTo().window(driver.getWindowHandle()); 
		driver.switchTo().frame("mainFrame");
		driver.findElement(By.id("mwx-ipt-username")).sendKeys(AdminEmail);
		driver.findElement(By.id("mwx-ipt-password")).sendKeys(pass);
		driver.findElement(By.id("mwx-btn-logon")).submit();
		driver.switchTo().window(driver.getWindowHandle());
		portal = driver.getWindowHandle();
		driver.switchTo().frame("header");
		WebElement link = driver.findElement(By.id("wcc-lnk-siteAdminLink"));
		Actions newwin = new Actions(driver);
		newwin.keyDown(Keys.SHIFT).click(link).keyUp(Keys.SHIFT).build().perform();
		for(String winHandle : driver.getWindowHandles()){
		    driver.switchTo().window(winHandle);
		    admin = winHandle;
		}
		//driver.findElement(By.cssSelector("a.sa_menu_icon")).click();
		driver.findElement(By.id("wcc-lnk-user")).click();
		wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.id("wcc-lnk-edituser"))));
		
	}

}

