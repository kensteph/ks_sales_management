package com.snack_bar.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.snack_bar.model.Employee;
import com.snack_bar.model.EmployeeFingerTemplate;
import com.snack_bar.model.FingerPrint;
import com.snack_bar.model.FingerPrintTemp;
import com.snack_bar.model.Item;
import com.snack_bar.model.Order;
import com.snack_bar.model.SaleItemListModel;
import com.snack_bar.util.Helper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private Context context;
    private final String TAG = "INIT_DB";
    private static final int databaseVersion = 1;
    private static final String databaseName = "POS1";
    private Helper helper;
    // Table Names
    private static final String TABLE_CATEGORIES = "categories";
    private static final String TABLE_SUB_CATEGORIES = "sub_categories";
    private static final String TABLE_PRODUCTS = "products";
    private static final String TABLE_SALES = "sales";
    private static final String TABLE_SALES_DETAILS = "sale_details";
    private static final String TABLE_EMPLOYEES = "employes";
    private static final String TABLE_FINGERPRINTS = "empreintes";
    private static final String TABLE_FINGERPRINTS_TMP = "empreintes_tmp";

    //SCRIPT
    String TB_CATEGORIES= "CREATE TABLE categories(" +
            "        id INTEGER," +
            "        category_name TEXT);";

    String TB_SUBCATEGORIES= "CREATE TABLE sub_categories(" +
            "id INTEGER ," +
            "        sub_category_name TEXT," +
            "        category_id INTEGER);";

    String TB_PRODUCTS= "CREATE TABLE products(" +
            "id INTEGER," +
            "        product_name TEXT," +
            "        sub_category_id INTEGER," +
            "        unit_price DOUBLE," +
            "        image TEXT);";

    String TB_SALES= "CREATE TABLE sales(" +
            "vente_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "        material_id TEXT," +
            "        employe_id INTEGER," +
            " vendeur_id INTEGER," +
            "        prix_total DOUBLE,"+
            "        date_vente DATETIME DEFAULT CURRENT_TIMESTAMP);";

    String TB_SALES_DETAILS= "CREATE TABLE sale_details(" +
            "vente_id INTEGER ," +
            "        produit_id INTEGER," +
            "        quantite INTEGER," +
            "        prix_unitaire DOUBLE);";

    String TB_EMPLOYEES=     "CREATE TABLE employes(" +
            "        employe_id INTEGER," +
            "        entreprise_id INTEGER," +
            "        employe_code TEXT," +
            "        employe_prenom TEXT," +
            "        employe_nom TEXT," +
            "        create_at DATETIME DEFAULT CURRENT_TIMESTAMP);" ;

    String TB_FINGERPRINTS=
            "        CREATE TABLE empreintes(" +
                    "        id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "        employe_id INTEGER," +
                    "        empreinte_nom TEXT," +
                    "        empreinte TEXT," +
                    "        template TEXT," +
                    "        create_at DATETIME DEFAULT CURRENT_TIMESTAMP);";
    String TB_FINGERPRINTS_TMP=
            "        CREATE TABLE empreintes_tmp(" +
                    "        id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "        employe_id INTEGER," +
                    "        empreinte_nom TEXT," +
                    "        empreinte TEXT," +
                    "        template TEXT," +
                    "        create_at DATETIME DEFAULT CURRENT_TIMESTAMP);";

    public DatabaseHelper(Context context) {
        super(context, databaseName, null, databaseVersion);
        this.context = context;
        helper=new Helper();
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(TAG,"TRYING TO CREATE DATABASE....");
        sqLiteDatabase.execSQL(TB_SALES);
        sqLiteDatabase.execSQL(TB_SALES_DETAILS);
        sqLiteDatabase.execSQL(TB_EMPLOYEES);
        sqLiteDatabase.execSQL(TB_FINGERPRINTS);
        sqLiteDatabase.execSQL(TB_CATEGORIES);
        sqLiteDatabase.execSQL(TB_SUBCATEGORIES);
        sqLiteDatabase.execSQL(TB_PRODUCTS);
        sqLiteDatabase.execSQL(TB_FINGERPRINTS_TMP);
        Log.d(TAG,"DATABASE CREATED SUCCESSFULLY....");
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // Drop older table if existed
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS sales");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS sale_details");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS categories");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS sub_categories");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS employes");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS empreintes");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS empreintes_tmp");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS products");
        onCreate(sqLiteDatabase);
    }
    //SAVE CATEGORIES
    public void saveCategories(int categoryID,String category_name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", categoryID);
        values.put("category_name", category_name);
        db.insert(TABLE_CATEGORIES, null, values);
        db.close();
    }
    //SAVE SUBCATEGORIES
    public void saveSubCategories(int subcategoryID,int categoryID,String sub_category_name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", subcategoryID);
        values.put("sub_category_name", sub_category_name);
        values.put("category_id", categoryID);

        db.insert(TABLE_SUB_CATEGORIES, null, values);
        db.close();
    }
    //SAVE PRODUCTS
    public void saveProducts(int productID,int subCategoryID,String productName,double unitPrice,String image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", productID);
        values.put("product_name", productName);
        values.put("sub_category_id", subCategoryID);
        values.put("unit_price", unitPrice);
        values.put("image", image);
        db.insert(TABLE_PRODUCTS, null, values);
        Log.d("INSERTION","INSERTION DONE");
        db.close();
    }
    //SAVE SALES
    public void saveSales(String materialID,int employeeID,int productID,int qty,double unitPrice,int cashier) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("material_id", materialID);
        values.put("employe_id", employeeID);
        values.put("produit_id", productID);
        values.put("quantite", qty);
        values.put("prix_unitaire", unitPrice);
        values.put("vendeur_id", cashier);
        db.insert(TABLE_SALES, null, values);
        db.close();
    }
    //SAVE SALE DETAILS | insert data using transaction and prepared statement
    public boolean saveSaleDetails(List<Order> saleDetails, int materialID, int employeeID, int cashier, Double totalPrice) {
        boolean done=false;
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransactionNonExclusive();
        try {
            // INSERT DATA TO SALES TB
            String query="INSERT INTO "+TABLE_SALES+" (material_id,employe_id,vendeur_id,prix_total) " +
                    " VALUES("+materialID+","+employeeID+","+cashier+","+totalPrice+")";
            db.execSQL(query);
            //GET ID SALE
            String query1 = "SELECT vente_id from sales order by vente_id DESC limit 1";
            Cursor c = db.rawQuery(query1,null);
            int saleID=0;
            if (c != null && c.moveToFirst()) {
                saleID = c.getInt(0); //The 0 is the column index, we only have 1 column, so the index is 0
            }
            Log.d("DB"," ORDERS : "+saleDetails.size());
            // INSERT THE DETAILS
            for(int i=0;i<saleDetails.size();i++){
                Order order = saleDetails.get(i);
                Item item =order.item;
                // INSERT DATA TO SALES TB
                String query3="INSERT INTO "+TABLE_SALES_DETAILS+" (vente_id,produit_id,quantite,prix_unitaire) " +
                        " VALUES("+saleID+","+item.id+","+order.quantity+","+item.unitPrice+")";
                db.execSQL(query3);
            }

            db.setTransactionSuccessful();
            db.endTransaction();
            done=true;
        } catch (Exception e){
            Log.d("DB","ERRORS ORDERS : "+e);
        }
        return done;
    }
    //SAVE EMPLOYEES
    public void saveEmployees(Employee employee) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("employe_id", employee.getEmployee_id());
        values.put("entreprise_id", employee.getEmployee_entreprise());
        values.put("employe_code", employee.getEmployee_code());
        values.put("employe_prenom", employee.getEmployee_prenom());
        values.put("employe_nom", employee.getEmployee_nom());
        db.insert(TABLE_EMPLOYEES, null, values);
        db.close();
    }
    //SAVE FINGERPRINTS
    public void saveFingerPrintsFromServer(int employeeId,String finger,byte[] fingerPrint,byte[] template) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("employe_id", employeeId);
        values.put("empreinte_nom", finger);
        values.put("empreinte", fingerPrint);
        values.put("template", template);
        db.insert(TABLE_FINGERPRINTS, null, values);
        db.close();
    }
    //DELETE TABLE INFO
    public boolean emptyTable(String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+ tableName);
        db.close();
        return true;
    }
    public List<Item> getProducts() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<Item> listItems = new ArrayList<Item>(); // Create an ArrayList object
        String selectQuery ="SELECT * FROM products  ORDER BY sub_category_id";
        Cursor cursor2 = db.rawQuery(selectQuery, null);
        while(cursor2.moveToNext()) {
            int id = cursor2.getInt(cursor2.getColumnIndexOrThrow("id"));
            String name= cursor2.getString(cursor2.getColumnIndexOrThrow("product_name"));
            double unitPrice = cursor2.getDouble(cursor2.getColumnIndexOrThrow("unit_price"));
            String image = cursor2.getString(cursor2.getColumnIndexOrThrow("image"));
            Item product = new Item(id,1,1,name,unitPrice,image);
            listItems.add(product);
        }

        cursor2.close();
        return listItems;
    }
    public List<SaleItemListModel> getAllSales() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<SaleItemListModel> saleList = new ArrayList<SaleItemListModel>(); // Create an ArrayList object
        String query="SELECT * FROM sales,employes WHERE sales.employe_id=employes.employe_id ORDER BY vente_id DESC";
        Cursor cursor2 = db.rawQuery(query, null);
        while(cursor2.moveToNext()) {
            SaleItemListModel sale = new SaleItemListModel();
            String dateV= cursor2.getString(cursor2.getColumnIndexOrThrow("date_vente"));
            double priceTotal = cursor2.getDouble(cursor2.getColumnIndexOrThrow("prix_total"));
            int saleId= cursor2.getInt(cursor2.getColumnIndexOrThrow("vente_id"));
            sale.setSaleDate(dateV);
            //SALES DETAILS
            List<Order> details =getSaleDetails(saleId);
            Log.d("DB","DESCRIPTION : "+details.size());
            String description="------------------------------------------------\n";
            for(Order order : details){
                Item product = order.item;
                description+=" "+order.quantity+" "+product.name+"  PRICE : "+product.unitPrice+"\n";
            }
            description+="------------------------------------------------\n";
            description+=" TOTAL : "+priceTotal+"\n";
            //INFO EMPLOYEE
            String prenom = cursor2.getString(cursor2.getColumnIndexOrThrow("employe_prenom"));
            String nom = cursor2.getString(cursor2.getColumnIndexOrThrow("employe_nom"));
            String code = cursor2.getString(cursor2.getColumnIndexOrThrow("employe_code"));
            int employe_id = cursor2.getInt(cursor2.getColumnIndexOrThrow("employe_id"));
            String full_name = saleId+" - "+prenom+" "+nom+" | "+employe_id;
            sale.setEmployeeName(full_name);
            sale.setEmployee(employe_id);
            sale.setCashier(cursor2.getInt(cursor2.getColumnIndexOrThrow("vendeur_id")));
            sale.setSaleDescription(description);
            sale.setExpandable(false);
            sale.setSaleId(saleId);
            sale.setItem(details);
            sale.setTotal(cursor2.getDouble(cursor2.getColumnIndexOrThrow("prix_total")));
            saleList.add(sale);
        }

        cursor2.close();
        return saleList;
    }
    //GET SALES DESCRIPTION
    public List<Order> getSaleDetails(int saleId) {
        SQLiteDatabase db = this.getWritableDatabase();
        List<Order> details = new ArrayList<>(); // Create an ArrayList object
        String query="SELECT * FROM sales,sale_details,products WHERE sales.vente_id=sale_details.vente_id AND products.id=sale_details.produit_id AND sales.vente_id="+saleId;
        Cursor cursor2 = db.rawQuery(query, null);
        while(cursor2.moveToNext()) {
            int id = cursor2.getInt(cursor2.getColumnIndexOrThrow("produit_id"));
            int category_id = cursor2.getInt(cursor2.getColumnIndexOrThrow("sub_category_id"));
            int sub_category_id = cursor2.getInt(cursor2.getColumnIndexOrThrow("sub_category_id"));
            String name= cursor2.getString(cursor2.getColumnIndexOrThrow("product_name"));
            double unitPrice = cursor2.getDouble(cursor2.getColumnIndexOrThrow("unit_price"));
            String image = cursor2.getString(cursor2.getColumnIndexOrThrow("image"));
            Item product = new Item(id,category_id,sub_category_id,name,unitPrice,image);
            int qty = cursor2.getInt(cursor2.getColumnIndexOrThrow("quantite"));
            Order orderLine = new Order(product,qty);
            details.add(orderLine);
        }

        cursor2.close();
        return details;
    }
    //GET SALES DESCRIPTION
    public boolean deleteSale(int saleId) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean done=false;
        String query="DELETE FROM sales WHERE vente_id="+saleId;
        db.execSQL(query);
        Log.d("DB",query);
        String query2="DELETE FROM sale_details WHERE vente_id="+saleId;
        db.execSQL(query2);
        Log.d("DB",query2);
        done=true;
        db.close();
        return done;
    }
    //NUMBER LINES  DETAILS FOUND
    public int getSalesDetailsCount() {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery ="SELECT COUNT(*) as tot FROM sale_details ";
        Cursor cursor2 = db.rawQuery(selectQuery, null);
        int nb=0;
        if(cursor2.moveToNext()) {
            nb = cursor2.getInt(cursor2.getColumnIndexOrThrow("tot"));
        }

        cursor2.close();
        return nb;
    }
    //DELETE SALES DETAILS
    public boolean deleteSaleDetails(int saleId,int productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean done=false;
        String query2="DELETE FROM sale_details WHERE vente_id="+saleId+" AND produit_id="+productId;
        db.execSQL(query2);
        Log.d("DB",query2);
        done=true;
        db.close();
        return done;
    }


    //ADD FINGERPRINT FOR EMPLOYEE
    public boolean addFingerPrint(byte[] imageByteArray, int employeeId,String finger) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("employe_id", employeeId);
        values.put("empreinte_nom", finger);
        byte[] serializeTemplate = helper.serializedTemplate(imageByteArray);
        values.put("empreinte", imageByteArray);
        values.put("template", serializeTemplate);
        boolean rep = db.insert(TABLE_FINGERPRINTS, null, values)>0;
        db.close();
        return rep;
    }
    //ADD FINGERPRINT FOR EMPLOYEE IN TB_FINGERPRINTS_TMP
    public boolean addTemporaryFingerPrint(List<FingerPrintTemp> fingerprints) {
        boolean rep=false;
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            for (FingerPrintTemp fp : fingerprints) {
                ContentValues values = new ContentValues();
                values.put("employe_id", fp.getEmployeeId());
                values.put("empreinte_nom", fp.getFinger());
                values.put("empreinte", fp.getFingerPrintImageBase64());
                values.put("template", fp.getFingerPrintTemplateBase64());

                db.insert(TABLE_FINGERPRINTS_TMP, null, values);
            }
            db.setTransactionSuccessful();
            rep = true;
        } finally {
            db.endTransaction();
            db.close();
        }
        return rep;
    }
    //FINGERPRINTS COUNT TO SYNC
    public int getFingerCount() {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery ="SELECT COUNT(*) as tot FROM empreintes_tmp ";
        Cursor cursor2 = db.rawQuery(selectQuery, null);
        int nb=0;
        if(cursor2.moveToNext()) {
            nb = cursor2.getInt(cursor2.getColumnIndexOrThrow("tot"));
        }

        cursor2.close();
        return nb;
    }
    //GET SALES DESCRIPTION
    public boolean deleteTemporaryFingerPrints(int employeeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean done=false;
        String query="DELETE FROM empreintes_tmp WHERE employe_id="+employeeId;
        db.execSQL(query);
        Log.d("DB",query);
        done=true;
        db.close();
        return done;
    }
    //GET INFO EMPLOYEE
    public List<FingerPrint> getAllFingersPrintsFromDB() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<FingerPrint> fingerPrintList = new ArrayList<FingerPrint>(); // Create an ArrayList object
        String query="SELECT * FROM employes,empreintes WHERE employes.employe_id=empreintes.employe_id";
        Cursor cursor2 = db.rawQuery(query, null);
        while(cursor2.moveToNext()) {
            FingerPrint fingerPrint = new FingerPrint();
            byte[] fp = cursor2.getBlob(cursor2.getColumnIndexOrThrow("empreinte"));
            byte[] template = cursor2.getBlob(cursor2.getColumnIndexOrThrow("template"));
            fingerPrint.setEmployeeId(cursor2.getInt(cursor2.getColumnIndexOrThrow("employe_id")));
            String prenom = cursor2.getString(cursor2.getColumnIndexOrThrow("employe_prenom"));
            String nom = cursor2.getString(cursor2.getColumnIndexOrThrow("employe_nom"));
            String code = cursor2.getString(cursor2.getColumnIndexOrThrow("employe_code"));
            String match = prenom+" "+nom;
            fingerPrint.setEmployeeFullName(match);
            fingerPrint.setEmployeeCode(code);
            fingerPrint.setFingerPrintByteArray(fp);
            fingerPrint.setFingerPrintTemplate(template);
            fingerPrintList.add(fingerPrint);
        }
        Log.d("FINGERPRINT1","FOUND : "+fingerPrintList.size());
        cursor2.close();
        return fingerPrintList;
    }
    //GET ALL EMPLOYEES FROM THE DB
    public List<Employee> getAllEmployeesFromDB() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<Employee> employeeList = new ArrayList<Employee>(); // Create an ArrayList object
        String query="SELECT * FROM employes";
        Cursor cursor2 = db.rawQuery(query, null);
        while(cursor2.moveToNext()) {
            String prenom = cursor2.getString(cursor2.getColumnIndexOrThrow("employe_prenom"));
            String nom = cursor2.getString(cursor2.getColumnIndexOrThrow("employe_nom"));
            String code = cursor2.getString(cursor2.getColumnIndexOrThrow("employe_code"));
            int idEmp = cursor2.getInt(cursor2.getColumnIndexOrThrow("employe_id"));
            int entreprise_id = cursor2.getInt(cursor2.getColumnIndexOrThrow("entreprise_id"));
            Employee employee = new Employee(idEmp,entreprise_id,code,prenom,nom);
            employeeList.add(employee);
        }
        Log.d("EMPLOYEE DATA","FOUND : "+employeeList.size());
        cursor2.close();
        return employeeList;
    }
    //GET ALL EMPLOYEES WITH NO FINGER PRINTS FROM THE DB
    public List<Employee> getEmployeesWithNoFingerPrintsFromDB() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<Employee> employeeList = new ArrayList<Employee>(); // Create an ArrayList object
        String query="SELECT * FROM employes";
        Cursor cursor2 = db.rawQuery(query, null);
        while(cursor2.moveToNext()) {
            String prenom = cursor2.getString(cursor2.getColumnIndexOrThrow("employe_prenom"));
            String nom = cursor2.getString(cursor2.getColumnIndexOrThrow("employe_nom"));
            String code = cursor2.getString(cursor2.getColumnIndexOrThrow("employe_code"));
            int idEmp = cursor2.getInt(cursor2.getColumnIndexOrThrow("employe_id"));
            int entreprise_id = cursor2.getInt(cursor2.getColumnIndexOrThrow("entreprise_id"));
            Employee employee = new Employee(idEmp,entreprise_id,code,prenom,nom);
            employeeList.add(employee);
        }
        Log.d("EMPLOYEE DATA","FOUND : "+employeeList.size());
        cursor2.close();
        return employeeList;
    }
    //GET SINGLE EMPLOYEES FROM THE DB
    public Employee getEmployeeInfo(int employeeID) {
        SQLiteDatabase db = this.getWritableDatabase();
        Employee employee = null;
        String query="SELECT * FROM employes WHERE employe_id="+employeeID;
        Cursor cursor2 = db.rawQuery(query, null);
        while(cursor2.moveToNext()) {
            String prenom = cursor2.getString(cursor2.getColumnIndexOrThrow("employe_prenom"));
            String nom = cursor2.getString(cursor2.getColumnIndexOrThrow("employe_nom"));
            String code = cursor2.getString(cursor2.getColumnIndexOrThrow("employe_code"));
            int idEmp = cursor2.getInt(cursor2.getColumnIndexOrThrow("employe_id"));
            int entreprise_id = cursor2.getInt(cursor2.getColumnIndexOrThrow("entreprise_id"));
            employee = new Employee(idEmp,entreprise_id,code,prenom,nom);
        }
        Log.d("EMPLOYEE DATA","FOUND : "+employee.toString());
        cursor2.close();
        return employee;
    }
    //GET ONLY FINGERPRINT TEMPLATE
    public List<EmployeeFingerTemplate> getFingersTemplate() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<EmployeeFingerTemplate> fingerPrintTemplates = new ArrayList<EmployeeFingerTemplate>(); // Create an ArrayList object
        String query="SELECT employe_id,template FROM empreintes";
        Cursor cursor2 = db.rawQuery(query, null);
        while(cursor2.moveToNext()) {
            byte[] template = cursor2.getBlob(cursor2.getColumnIndexOrThrow("template"));
            int employeeId = cursor2.getInt(cursor2.getColumnIndexOrThrow("employe_id"));
            EmployeeFingerTemplate fpTemplate = new EmployeeFingerTemplate(employeeId,template);
            fingerPrintTemplates.add(fpTemplate);
        }
        Log.d("FINGERPRINT1","FOUND : "+fingerPrintTemplates.size());
        cursor2.close();
        return fingerPrintTemplates;
    }

    //GET TEMPORARY FINGERPRINTS IN DB
    public List<FingerPrintTemp> getTemporaryFingers() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<FingerPrintTemp> fingerPrintTmp = new ArrayList<>(); // Create an ArrayList object
        String query="SELECT * FROM empreintes_tmp";
        Cursor cursor2 = db.rawQuery(query, null);
        while(cursor2.moveToNext()) {
            String templateBase64 = cursor2.getString(cursor2.getColumnIndexOrThrow("template"));
            String fingerPrintImageBase64 = cursor2.getString(cursor2.getColumnIndexOrThrow("empreinte"));
            String finger = cursor2.getString(cursor2.getColumnIndexOrThrow("empreinte_nom"));
            int employeeId = cursor2.getInt(cursor2.getColumnIndexOrThrow("employe_id"));
            FingerPrintTemp fp = new FingerPrintTemp();
            fp.setEmployeeId(employeeId);
            fp.setFingerPrintTemplateBase64(templateBase64);
            fp.setFinger(finger);
            fp.setFingerPrintImageBase64(fingerPrintImageBase64);
            fingerPrintTmp.add(fp);
        }
        Log.d("FINGERPRINT1","FOUND : "+fingerPrintTmp.size());
        cursor2.close();
        return fingerPrintTmp;
    }

}
