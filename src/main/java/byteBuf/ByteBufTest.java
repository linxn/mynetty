package byteBuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

//仔细看 仔细理解
public class ByteBufTest {
    public static void main(String[] args) {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(9, 100);

        print("allocate ByteBuf(9, 100)", buffer);

        //write 方法改变写指针 写完后写指针未到capacity的时候，buffer仍然可以写
        buffer.writeBytes(new byte[]{1, 2, 3, 4});
        print("writeBytes(1, 2, 3, 4)", buffer);

        //写完后 写指针增加4
        buffer.writeInt(12);
        print("buffer.writeInt(12)", buffer);

        //write方法改变写指针，写完之后 写指针等于capacity的时候，buffer不可写
        buffer.writeBytes(new byte[]{5});
        print("writeBytes(5)", buffer);

        //write方法改变写指针，写的时候发现buffer不可写则开始扩容，扩容后capacity随即改变
        //扩容：calculateNewCapacity 方法     从64B开始，指数扩容
        buffer.writeBytes(new byte[]{6});
        print("writeBytes(6)", buffer);

        //get 方法不改变读写指针
        System.out.println("getByte(3) return: " + buffer.getByte(3));
        System.out.println("getShort(3) return: " + buffer.getShort(3));
        System.out.println("getInt(3) return: " + buffer.getInt(3));
        print("getByte()", buffer);

        //set 方法不改变读写指针
        buffer.setByte(buffer.readableBytes() + 1, 0);
        print("setByte()", buffer);

        byte[] dst = new byte[buffer.readableBytes()];
        buffer.readBytes(dst);
        print("readBytes(" + dst.length + ")", buffer);
    }

    private static void print(String action, ByteBuf buffer) {
        System.out.println("after =========" + action + "===========");
        System.out.println("capacity(): " + buffer.capacity());
        System.out.println("maxCapacity(): " + buffer.maxCapacity());
        System.out.println("readerIndex(): " + buffer.readerIndex());
        System.out.println("readableBytes(): " + buffer.readableBytes());
        System.out.println("isReadable(): " + buffer.isReadable());
        System.out.println("writerIndex(): " + buffer.writerIndex());
        System.out.println("writableBytes(): " + buffer.writableBytes());
        System.out.println("isWritable(): " + buffer.isWritable());
        System.out.println("maxWritableBytes(): " + buffer.maxWritableBytes());
        System.out.println();
    }

    /*
     * 1. netty使用堆外内存，不被JVM直接管理，需要手动释放内存，否则会造成内存泄漏
     * 2. Netty的ByteBuf是通过引用计数的方式管理的
     *         ·每次调用retain()方法 引用加一
     *         ·每次调用release()方法 引用减一
     *         ·如果引用计数为0，则直接回收ByteBuf底层的内存
     * 3. slice()、duplicate()、copy()这三者的返回值都是一个新的ByteBuf对象
     * 4. slice() 与 duplicate() 相同点：都维持着与原始ByteBuf相同的内存引用计数和不同的读写指针
     * 5. copy() 会拷贝所有信息，所以引用计数也复制了
     * 6. 原ByteBuf的引用计数改变，slice() 和 duplicate() 复制的ByteBuf会受到影响（比如内存被回收了），而copy()出来的ByteBuf没有影响
     * 7. 所以，当要对slice()、duplicate() 的 ByteBuf 进行操作的时候，可以先retain()来增加引用，之后再调用release()
     * 8. 这三个方法都维护自己的读写指针，与原 ByteBuf 的读写指针无关，相互之间不受影响
     * 9. 简单而言： copy是深拷贝，slice和duplicate是浅复制，slice不能写，duplicate进行写操作会影响原ByteBuf
     *
     * */
}

