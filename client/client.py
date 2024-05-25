import tkinter as tk
from tkinter import font
from socket import *

serverName = '192.168.43.108'  #替换为你的服务器名或IP地址
serverPort = 2525

clientSocket = socket(AF_INET, SOCK_STREAM)
clientSocket.connect((serverName, serverPort))  # 确保使用英文逗号

# 第二个UI窗口的类
class SecondWindow(tk.Toplevel):
    def __init__(self, master):
        super().__init__(master)
        self.title("进入界面")
        self.geometry("600x400")
        self.configure(bg='#f0f0f0')  # 设置窗口背景色

        # 设置字体样式
        self.default_font = font.Font(family='Arial', size=12)

        # 标题标签
        self.label = tk.Label(self, text="请选择您要办理的业务", font=self.default_font, bg='#f0f0f0')
        self.label.place(x=50, y=50, width=300, height=50)

        # 输入框
        self.entry = tk.Entry(self, font=self.default_font)
        self.entry.place(x=120, y=180, width=200, height=30)

        # 按钮
        self.button1 = tk.Button(self, text="查询余额", command=self.on_button1_click,
                                 font=self.default_font, bg='#d0d0d0', fg='black', cursor='hand2')
        self.button1.place(x=450, y=50, width=100, height=50)

        self.button2 = tk.Button(self, text="取款", command=self.on_button2_click,
                                 font=self.default_font, bg='#d0d0d0', fg='black', cursor='hand2')
        self.button2.place(x=450, y=125, width=100, height=50)

        self.button3 = tk.Button(self, text="退出系统", command=self.on_button3_click,
                                 font=self.default_font, bg='#d0d0d0', fg='black', cursor='hand2')
        self.button3.place(x=450, y=200, width=100, height=50)

        # 当UI2窗口关闭时，通知主窗口
        self.protocol("WM_DELETE_WINDOW", self.on_close)

    def on_button1_click(self):
        text1 = "BALA"
        clientSocket.send(text1.encode())  # 发送前编码为字节流

        modifiedSentence = clientSocket.recv(1024)
        sentence = modifiedSentence.decode()  # 接收后解码为字符串
        print(sentence)
        substring = sentence[5:]
        print(substring)

        self.label.config(text="您当前的余额为："+substring+"元")


    def on_button2_click(self):
        text1 = "WDRA " + self.entry.get()
        clientSocket.send(text1.encode())  # 发送前编码为字节流

        modifiedSentence = clientSocket.recv(1024)
        sentence = modifiedSentence.decode()  # 接收后解码为字符串

        if sentence == "525 OK!":
            self.label.config(text="恭喜您，取款成功，共计"+self.entry.get()+"元！")
        elif sentence == "401 ERROR!":
            self.label.config(text="您输入的金额有误，请再次输入！")
    def on_button3_click(self):

        text1 = "BYE"
        clientSocket.send(text1.encode())  # 发送前编码为字节流

        # 通知主窗口，可以重新处理事件了
        self.master.event_generate("<<SecondWindowClosed>>")
        # 关闭当前窗口
        self.destroy()

    def on_close(self):
        # 在这里处理窗口关闭事件
        print("窗口正在关闭...")
        # 通知主窗口 SecondWindow 已经关闭
        self.event_generate("<<SecondWindowClosed>>", when="tail")
        self.destroy()  # 销毁窗口

    #

    # 第一个UI窗口的类


class MainWindow(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("登录界面")
        self.geometry("600x400")
        self.configure(bg='#f0f0f0')  # 设置窗口背景色

        # 设置字体样式
        self.default_font = font.Font(family='Arial', size=12)

        # 标题标签
        self.label = tk.Label(self, text="请输入您的信息", font=self.default_font, bg='#f0f0f0')
        self.label.place(x=50, y=50, width=300, height=50)

        # 输入框
        self.entry = tk.Entry(self, font=self.default_font)
        self.entry.place(x=50, y=120, width=500, height=30)

        # 按钮
        self.button1 = tk.Button(self, text="输入卡号", command=self.on_button1_click,
                                 font=self.default_font, bg='#d0d0d0', fg='black', cursor='hand2')
        self.button1.place(x=450, y=50, width=100, height=50)

        self.button2 = tk.Button(self, text="输入密码", command=self.on_button2_click,
                                 font=self.default_font, bg='#d0d0d0', fg='black', cursor='hand2')
        self.button2.place(x=450, y=100, width=100, height=50)

        # 绑定自定义事件，用于在SecondWindow关闭后重新启用控件
        self.bind("<<SecondWindowClosed>>", self.on_second_window_closed)

    def on_button1_click(self):
        text1 = "HELO "+self.entry.get()

        clientSocket.send(text1.encode())  # 发送前编码为字节流

        #modifiedSentence = clientSocket.recv(1024)
        modifiedSentence = clientSocket.recv(1024)
        sentence = modifiedSentence.decode()  # 接收后解码为字符串
        print(sentence)


        if sentence == "500 AUTH REQUIRE":
            self.label.config(text="请在下方输入密码")
        elif sentence == "401 ERROR!":
            self.label.config(text="该账号无效！")
        else:
            self.label.config(text="未知响应")  #

        self.entry.delete(0, 'end')


    def on_button2_click(self):


        text1 = "PASS "+self.entry.get()
        clientSocket.send(text1.encode())  # 发送前编码为字节流

        modifiedSentence = clientSocket.recv(1024)
        sentence = modifiedSentence.decode()  # 接收后解码为字符串
        print(sentence)

        if sentence == "525 OK!":
             #创建并显示第二个UI窗口
             self.disable_controls()  # 禁用主窗口的部分控件
             self.second_window = SecondWindow(self)

        elif sentence == "401 ERROR!":
            self.label.config(text="您输入的密码错误，请再次输入！")
        else:
            self.label.config(text="未知响应2")  #


    def disable_controls(self):
        # 禁用主窗口中的Button和Entry控件
        for widget in (self.button1, self.button2, self.entry):
            widget.config(state='disabled')

    def enable_controls(self):
        # 启用主窗口中的Button和Entry控件
        for widget in (self.button1, self.button2, self.entry):
            widget.config(state='normal')

    def on_second_window_closed(self, event):
        # 当SecondWindow关闭后，重新启用主窗口的控件
        self.enable_controls()

    # 创建并显示第一个UI窗口


app = MainWindow()
app.mainloop()