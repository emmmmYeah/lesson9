package com.example.exp0405

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    val  TAG="@@MainActivity"

    lateinit var helper:DBHelper
    lateinit var adapter:TodoAdapter

    private  var toUpdate:Todo?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init(){
        //打开DB
        helper = DBHelper(this,"todo.db",3)
        adapter=TodoAdapter()
//下面这三行是要用adapter一定要写的
        val recycler=findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager=LinearLayoutManager(this)
        recycler.adapter=adapter
    //
        readInDb()
        findViewById<TextView>(R.id.btn_save).setOnClickListener{
            saveInput()
        }
    }

    private fun saveInput() {
        //取出字符串并转向saveInDb()中
        val text=findViewById<EditText>(R.id.ipt_text).text.toString()
        saveInDb(text)
    }

    private fun saveInDb(text: String) {
        //往数据库存数据！！！
        val db=helper.writableDatabase
        val item=Todo(text,System.currentTimeMillis())
        val values=ContentValues().apply {
            put(Todo.COL_CONTENT,item.content)
            put(Todo.COL_TIME,item.createTime)
        }
        var rs=-1

        if(toUpdate!=null) {
            item.id=toUpdate?.id
            Log.d(TAG,"UPDATE ID=$rs")
            rs=db.update(Todo.TABLE,values,"id=?", arrayOf(toUpdate?.id.toString()))
            if(rs!=-1){
                toUpdate?.id?.let { adapter.replaceItem(it,item) }
                toUpdate=null
        }
            else{
                Log.d(TAG,"insert id =$rs")
                rs=db.insert(Todo.TABLE,null,values).toInt()
                if(rs!=-1){
                    item.id=rs
                    adapter.addItem(item)
                }
            }
        }

        //一定要置空！！
        setInputText("")

    }

    private fun setInputText(s: String) {
        findViewById<EditText>(R.id.ipt_text).setText(s)

    }


    private fun readInDb() {
        val db = helper.readableDatabase//文件为只读
        val cursor = db.query(Todo.TABLE,null,null,null,null,null,
            "${Todo.COL_ID} desc ")
        val arr = arrayListOf<Todo>()
        if(cursor.moveToFirst()){
            do{//遍历Cursor对象，取出数据并打印
                arr.add(
                    Todo(
                        //这里需要加个判断才行
                        cursor.getString(cursor.getColumnIndex(Todo.COL_CONTENT)),
                        cursor.getLong(cursor.getColumnIndex(Todo.COL_TIME)),
                    ).apply {
                        id = cursor.getInt(cursor.getColumnIndex(Todo.COL_ID))
                    }
                )
            }while (cursor.moveToNext())
        }

        adapter.setData(arr)

        cursor.close()
    }
//这个是使用recycler最重要的部分
  inner class TodoAdapter: RecyclerView.Adapter<TodoViewHolder>() {
    val cont= arrayListOf<Todo>()//这个是getItemCount重要返还的数据大小的数据

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
           //在实际开发种LayoutInflater这个类还是非常有用的，它的作用类似于findViewById()，不同点是LayoutInflater是用来找layout下xml布局文件，并且实例化！而findViewById()是找具体xml下的具体widget控件(如:Button,TextView等)。
           val view= LayoutInflater.from(parent.context).inflate(R.layout.item_todo,parent,false)
            return TodoViewHolder(view).apply {
                id=view.findViewById(R.id.id)
                content=view.findViewById(R.id.content)
                btnUpdate=view.findViewById(R.id.btn_update)
                btnDelete=view.findViewById(R.id.btn_delete)
            }
            //  val holder=TeacherViewHolder(view)EXP0320中的写法,
            //        return holder
            //下面是EXP0320中TeacherViewHolder中的内容，和我这个有区别在于在HOlder中就已经找好val值了而我这里是在OnCreat里才找的
            //val name=itemView.findViewById<TextView>(R.id.name)
            //         val desc=itemView.findViewById<TextView>(R.id.description)
            //         val img=itemView.findViewById<ImageView>(R.id.avater)
//这个item需要我们用inflate函数把msg_item动态的加载进main布局，
//并且返回了一个用来获取item里控件并且对其进行操作的View对象。
        }

        override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
           //束缚控件的位置
            holder.render(cont[position])
        }

        override fun getItemCount(): Int {
            return cont.size
        }

    fun setData(arr: ArrayList<Todo>) {
        cont.addAll(arr)
        notifyDataSetChanged()
    }

    fun replaceItem(id: Int, item: Todo) {
        val idx = findIdx(id)
        if(idx >= 0) {
            cont.set(idx, item)
            notifyItemChanged(idx)
        }
    }

    fun addItem(item: Todo) {
        cont.add(0,item)
        notifyItemInserted(0)
    }
    private fun findIdx(id:Int?):Int{
        var idx=-1
        cont.forEachIndexed{ index,todo-?
            if(todo.id==id){
                idx=index
            }
        }
        return idx
    }

}

    inner class TodoViewHolder(view: View):RecyclerView.ViewHolder(view) {

        var id:TextView?=null
        var content:TextView?=null
        var btnUpdate:TextView?=null
        var btnDelete:TextView?=null
        fun render(todo:Todo) {
            id?.text=todo.id.toString()
            content?.text=todo.content

            btnDelete?.setOnClickListener{

            }
            btnUpdate?.setOnClickListener {

            }
        }
    }



}