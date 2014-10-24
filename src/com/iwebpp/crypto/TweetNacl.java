package com.iwebpp.crypto;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;

public final class TweetNacl {

	/*
static byte
  _0[16],
  _9[32] = {9};
	 */
	private static final byte [] _0 = new byte[16];
	private static final byte [] _9 = new byte[32];
	static {
		for (int i = 0; i < _0.length; i ++) _0[i] = 0;
		for (int i = 0; i < _9.length; i ++) _9[i] = 9;
	}

	/*
static gf
  gf0,
  gf1 = {1},
  _121665 = {0xDB41,1},
  D = {0x78a3, 0x1359, 0x4dca, 0x75eb, 0xd8ab, 0x4141, 0x0a4d, 0x0070, 0xe898, 0x7779, 0x4079, 0x8cc7, 0xfe73, 0x2b6f, 0x6cee, 0x5203},
  D2 = {0xf159, 0x26b2, 0x9b94, 0xebd6, 0xb156, 0x8283, 0x149a, 0x00e0, 0xd130, 0xeef3, 0x80f2, 0x198e, 0xfce7, 0x56df, 0xd9dc, 0x2406},
  X = {0xd51a, 0x8f25, 0x2d60, 0xc956, 0xa7b2, 0x9525, 0xc760, 0x692c, 0xdc5c, 0xfdd6, 0xe231, 0xc0a4, 0x53fe, 0xcd6e, 0x36d3, 0x2169},
  Y = {0x6658, 0x6666, 0x6666, 0x6666, 0x6666, 0x6666, 0x6666, 0x6666, 0x6666, 0x6666, 0x6666, 0x6666, 0x6666, 0x6666, 0x6666, 0x6666},
  I = {0xa0b0, 0x4a0e, 0x1b27, 0xc4ee, 0xe478, 0xad2f, 0x1806, 0x2f43, 0xd7a7, 0x3dfb, 0x0099, 0x2b4d, 0xdf0b, 0x4fc1, 0x2480, 0x2b83};
	 */
	private static final long [] gf0 = new long[16];
	private static final long [] gf1 = new long[16];
	private static final long [] _121665 = new long[16];
	static {
		for (int i = 0; i < gf0.length; i ++) gf0[i] = 0;
		for (int i = 0; i < gf1.length; i ++) gf1[i] = 1;
		for (int i = 0; i < _121665.length/2; i ++) {
			_121665[i] = 0xDB41;
			_121665[i+1] = 1;
		}
	}

	private static final long []  D = new long [] {0x78a3, 0x1359, 0x4dca, 0x75eb, 0xd8ab, 0x4141, 0x0a4d, 0x0070, 0xe898, 0x7779, 0x4079, 0x8cc7, 0xfe73, 0x2b6f, 0x6cee, 0x5203};
	private static final long [] D2 = new long [] {0xf159, 0x26b2, 0x9b94, 0xebd6, 0xb156, 0x8283, 0x149a, 0x00e0, 0xd130, 0xeef3, 0x80f2, 0x198e, 0xfce7, 0x56df, 0xd9dc, 0x2406};
	private static final long []  X = new long [] {0xd51a, 0x8f25, 0x2d60, 0xc956, 0xa7b2, 0x9525, 0xc760, 0x692c, 0xdc5c, 0xfdd6, 0xe231, 0xc0a4, 0x53fe, 0xcd6e, 0x36d3, 0x2169};
	private static final long []  Y = new long [] {0x6658, 0x6666, 0x6666, 0x6666, 0x6666, 0x6666, 0x6666, 0x6666, 0x6666, 0x6666, 0x6666, 0x6666, 0x6666, 0x6666, 0x6666, 0x6666};
	private static final long []  I = new long [] {0xa0b0, 0x4a0e, 0x1b27, 0xc4ee, 0xe478, 0xad2f, 0x1806, 0x2f43, 0xd7a7, 0x3dfb, 0x0099, 0x2b4d, 0xdf0b, 0x4fc1, 0x2480, 0x2b83};



	///static int L32(int x,int c) { return (x << c) | ((x&0xffffffff) >> (32 - c)); }
	private static int L32(int x, int c) { return (x << c) | ((x&0xffffffff) >> (32 - c)); }

	/*
private static int ld32(byte [] x)
{
  int u = x[3];
  u = (u<<8)|x[2];
  u = (u<<8)|x[1];
  return (u<<8)|x[0];
}*/
	private static int ld32(ByteBuffer x)
	{
		int u = x.get(3);
		u = (u<<8)|x.get(2);
		u = (u<<8)|x.get(1);
		return (u<<8)|x.get(0);
	}

	///private static long dl64(byte []x)
	private static long dl64(ByteBuffer x)
	{
		int i;
		long u=0;
		///FOR(i,8) u=(u<<8)|x[i];
		for (i = 0; i < 8; i ++) u=(u<<8)|x.get(i);///x[i];
		return u;
	}

	private static void st32(ByteBuffer x, int u)
	{
		int i;
		for (i = 0; i < 4; i ++) { x.put(i, (byte) u); u >>= 8; }
	}

	///private static void ts64(byte []x,long u)
	private static void ts64(ByteBuffer x,long u)
	{
		int i;
		for (i = 7;i >= 0;--i) { x.put(i, (byte) u)/*x[i] = (byte) u*/; u >>= 8; }
	}

	private static int vn(ByteBuffer x, ByteBuffer y,int n)
	{
		int i,d = 0;
		for (i = 0; i < n; i ++) d |= x.get(i)^y.get(i);///x[i]^y[i];
		return (1 & ((d - 1) >> 8)) - 1;
	}

	public static int crypto_verify_16(ByteBuffer x, ByteBuffer y)
	{
		return vn(x,y,16);
	}

	public static int crypto_verify_32(ByteBuffer x, ByteBuffer y)
	{
		return vn(x,y,32);
	}

	private static void core(byte [] out, byte [] in, byte [] k, byte [] c, int h)
	{
		///int w[16],x[16],y[16],t[4];
		int [] w = new int[16], x = new int[16], y = new int[16], t = new int[4]; 
		int i,j,m;

		///FOR(i,4) {
		for (i = 0; i < 4; i ++) {
			x[5*i] = ld32(ByteBuffer.wrap(c, 4*i, 4));
			x[1+i] = ld32(ByteBuffer.wrap(k, 4*i, 4));
			x[6+i] = ld32(ByteBuffer.wrap(in, 4*i, 4));
			x[11+i] = ld32(ByteBuffer.wrap(k, 16+4*i, 4));
		}

		///FOR(i,16) y[i] = x[i];
		for (i = 0; i < 16; i ++) y[i] = x[i];

		///FOR(i,20) {
		for (i = 0; i < 20; i ++) {
			///FOR(j,4) {
			for (j = 0; j < 4; j ++) {
				///FOR(m,4) t[m] = x[(5*j+4*m)%16];
				for (m = 0; m < 4; m ++) t[m] = x[(5*j+4*m)%16];
				t[1] ^= L32(t[0]+t[3], 7);
				t[2] ^= L32(t[1]+t[0], 9);
				t[3] ^= L32(t[2]+t[1],13);
				t[0] ^= L32(t[3]+t[2],18);
				///FOR(m,4) w[4*j+(j+m)%4] = t[m];
				for (m = 0; m < 4; m ++) w[4*j+(j+m)%4] = t[m];
			}
			///FOR(m,16) x[m] = w[m];
			for (m = 0; m < 16; m ++) x[m] = w[m];
		}

		if (h != 0) {
			///FOR(i,16) x[i] += y[i];
			for (i = 0; i < 16; i ++) x[i] += y[i];
			///FOR(i,4) {
			for (i = 0; i < 4; i ++) {
				x[5*i] -= ld32(ByteBuffer.wrap(c, 4*i, 4));///ld32(c+4*i);
				x[6+i] -= ld32(ByteBuffer.wrap(in, 4*i, 4));///ld32(in+4*i);
			}
			///FOR(i,4) {
			for (i = 0; i < 4; i ++) {
				st32(ByteBuffer.wrap(out, 4*i, 4), x[5*i]);///st32(out+4*i, x[5*i]);
				st32(ByteBuffer.wrap(out, 16+4*i, 4), x[6+i]);///st32(out+16+4*i, x[6+i]);
			}
		} else
			///FOR(i,16) st32(out + 4 * i,x[i] + y[i]);
			for (i = 0; i < 16; i ++) st32(ByteBuffer.wrap(out, 4*i, 4), x[i] + y[i]);///st32(out + 4 * i,x[i] + y[i]);
	}

	int crypto_core_salsa20(byte [] out, byte [] in, byte [] k, byte [] c)
	{
		core(out,in,k,c,0);
		return 0;
	}

	int crypto_core_hsalsa20(byte [] out, byte [] in, byte [] k, byte [] c)
	{
		core(out,in,k,c,1);
		return 0;
	}

	private static byte[] sigma;
	static {
		try {
			sigma = "expand 32-byte k".getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	int crypto_stream_salsa20_xor(byte [] c, byte [] m, long b, ByteBuffer n, byte [] k)
	{
		byte[] z = new byte[16], x = new byte[64];
		int u,i;
		if (0==b) return 0;

		///FOR(i,16) z[i] = 0;
		///FOR(i,8) z[i] = n[i];
		for (i = 0; i < 16; i ++) z[i] = 0;
		for (i = 0; i < 8; i ++) z[i] = n.get(i);///n[i];

		int coffset = 0;
		int moffset = 0;
		while (b >= 64) {
			crypto_core_salsa20(x,z,k,sigma);
			for (i = 0; i < 64; i ++) c[i+coffset] = (byte) ((m!=null?m[i+moffset]:0) ^ x[i]);
			u = 1;
			for (i = 8;i < 16;++i) {
				u += (int) z[i];
				z[i] = (byte) u;
				u >>= 8;
			}
			b -= 64;
			///c += 64;
			coffset += 64;
			if (m!=null) moffset += 64;///m += 64;
		}
		if (b!=0) {
			crypto_core_salsa20(x,z,k,sigma);
			for (i = 0; i < b; i ++) c[i+coffset] = (byte) ((m!=null?m[i+moffset]:0) ^ x[i]);
		}
		return 0;
	}

	int crypto_stream_salsa20(byte [] c, long d, ByteBuffer n, byte [] k)
	{
		return crypto_stream_salsa20_xor(c,null,d,n,k);
	}

	int crypto_stream(byte [] c, long d, byte [] n, byte [] k)
	{
		byte[] s = new byte[32];///s[32];
		crypto_core_hsalsa20(s,n,k,sigma);
		return crypto_stream_salsa20(c,d,ByteBuffer.wrap(n, 16, n.length-16),s);///crypto_stream_salsa20(c,d,n+16,s);
	}

	int crypto_stream_xor(byte []c,byte []m,long d,byte []n,byte []k)
	{
		byte[] s = new byte[32];///s[32];
		crypto_core_hsalsa20(s,n,k,sigma);
		return crypto_stream_salsa20_xor(c,m,d,ByteBuffer.wrap(n, 16, n.length-16),s);///crypto_stream_salsa20_xor(c,m,d,n+16,s);
	}

	private static void add1305(int [] h,int [] c)
	{
		int j,u = 0;
		///FOR(j,17) {
		for (j = 0; j < 17; j ++) {
			u += h[j] + c[j];
			h[j] = u & 255;
			u >>= 8;
		}
	}

	private static int minusp[] = {
		5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 252
	} ;

	int crypto_onetimeauth(ByteBuffer out, ByteBuffer m, long n , byte [] k)
	{
		int s,i,j,u;
		int [] x = new int[17], r = new int [17], 
				h = new int[17], c = new int [17], g = new int[17];

		///FOR(j,17) r[j]=h[j]=0;
		for (j = 0; j < 17; j ++) r[j] = h[j] = 0;
		///FOR(j,16) r[j]=k[j];
		for (j = 0; j < 16; j ++) r[j] = k[j];
		r[3]&=15;
		r[4]&=252;
		r[7]&=15;
		r[8]&=252;
		r[11]&=15;
		r[12]&=252;
		r[15]&=15;

		int moffset = 0;
		while (n > 0) {
			///FOR(j,17) c[j] = 0;
			for (j = 0; j < 17; j ++) c[j] = 0;
			for (j = 0;(j < 16) && (j < n);++j) c[j] = m.get(j+moffset);///m[j+moffset];
			c[j] = 1;

			moffset += j;///m += j; 

			n -= j;
			add1305(h,c);
			///FOR(i,17) {
			for (i = 0; i < 17; i ++) {
				x[i] = 0;
				///FOR(j,17) 
				for (j = 0; j < 17; j ++) x[i] += h[j] * ((j <= i) ? r[i - j] : 320 * r[i + 17 - j]);
			}
			///FOR(i,17) 
			for (i = 0; i < 17; i ++) h[i] = x[i];
			u = 0;
			///FOR(j,16) {
			for (j = 0; j < 16; j ++) {
				u += h[j];
				h[j] = u & 255;
				u >>= 8;
			}
			u += h[16]; h[16] = u & 3;
			u = 5 * (u >> 2);
			///FOR(j,16) {
			for (j = 0; j < 16; j ++) {
				u += h[j];
				h[j] = u & 255;
				u >>= 8;
			}
			u += h[16]; h[16] = u;
		}

		///FOR(j,17) 
		for (j = 0; j < 17; j ++) g[j] = h[j];
		add1305(h,minusp);
		s = -(h[16] >> 7);
		///FOR(j,17) 
		for (j = 0; j < 17; j ++) h[j] ^= s & (g[j] ^ h[j]);

		///FOR(j,16) 
		for (j = 0; j < 16; j ++) c[j] = k[j + 16];
		c[16] = 0;
		add1305(h,c);
		///FOR(j,16) 
		for (j = 0; j < 16; j ++) out.put(j, (byte) h[j]);///out[j] = h[j];
		return 0;
	}

	int crypto_onetimeauth_verify(ByteBuffer h, ByteBuffer m, long n, byte [] k)
	{
		byte[] x = new byte[16];
		crypto_onetimeauth(ByteBuffer.wrap(x),m,n,k);
		return crypto_verify_16(h,ByteBuffer.wrap(x));
	}

	int crypto_secretbox(byte [] c, byte [] m, long d, byte [] n, byte [] k)
	{
		int i;
		if (d < 32) return -1;
		crypto_stream_xor(c,m,d,n,k);
		crypto_onetimeauth(ByteBuffer.wrap(c, 16, c.length-16)/*c + 16*/, ByteBuffer.wrap(c, 32, c.length-32)/*c + 32*/, d - 32, c);
		for (i = 0; i < 16; i ++) c[i] = 0;
		return 0;
	}

	int crypto_secretbox_open(byte []m,byte []c,long d,byte []n,byte []k)
	{
		int i;
		byte[] x = new byte[32];
		if (d < 32) return -1;
		crypto_stream(x,32,n,k);
		if (crypto_onetimeauth_verify(ByteBuffer.wrap(c, 16, 16)/*c + 16*/, ByteBuffer.wrap(c, 32, c.length-32)/*c + 32*/, d - 32, x) != 0) return -1;
		crypto_stream_xor(m,c,d,n,k);
		///FOR(i,32)
		for (i = 0; i < 32; i ++) m[i] = 0;
		return 0;
	}

	///private static void set25519(gf r, gf a)
	private static void set25519(long [] r, long [] a)///(long[16] r, long[16] a)
	{
		int i;
		///FOR(i,16) 
		for (i = 0; i < 16; i ++) r[i]=a[i];
	}

	///private static void car25519(gf o)
	private static void car25519(LongBuffer o)
	{
		int i;
		long c;
		///FOR(i,16) 
		for (i = 0; i < 16; i ++) {
			///o[i]+=(1L<<16);
			o.put(i, o.get(i)+(1L<<16));

			///c=o[i]>>16;
			c = o.get(i)>>16;

			///o[(i+1)*((i<15) ? 1 : 0)]+=c-1+37*(c-1)*((i==15) ? 1 : 0);
			int idx = (i+1)*((i<15) ? 1 : 0);
			o.put(idx, o.get(idx) + c-1+37*(c-1)*((i==15) ? 1 : 0));

			///o[i]-=c<<16;
			o.put(i, o.get(i) - c<<16);
		}
	}

	///private static void sel25519(gf p,gf q,int b)
	private static void sel25519(LongBuffer p, LongBuffer q, int b)
	{
		int i;
		long t,c=~(b-1);
		///FOR(i,16) {
		for (i = 0; i < 16; i ++) {
			///t= c&(p[i]^q[i]);
			t = c & (p.get(i) ^ q.get(i));

			///p[i]^=t;
			p.put(i, p.get(i) ^ t);

			///q[i]^=t;
			q.put(i, q.get(i) ^ t);
		}
	}

	///private static void pack25519(byte [] o, gf n)
	private static void pack25519(byte [] o, LongBuffer n)
	{
		int i,j,b;
		///gf m,t;
		long [] m = new long[16], t = new long[16];
		///FOR(i,16) t[i]=n[i];
		for (i = 0; i < 16; i ++) t[i] = n.get(i);///n[i];

		LongBuffer tb = LongBuffer.wrap(t);
		car25519(tb);
		car25519(tb);
		car25519(tb);
		///FOR(j,2) {
		for (j = 0; j < 2; j ++) {
			m[0]=t[0]-0xffed;
			for(i=1;i<15;i++) {
				m[i]=t[i]-0xffff-((m[i-1]>>16)&1);
				m[i-1]&=0xffff;
			}
			m[15]=t[15]-0x7fff-((m[14]>>16)&1);
			b=(int) ((m[15]>>16)&1);
			m[14]&=0xffff;
			sel25519(LongBuffer.wrap(t),LongBuffer.wrap(m),1-b);
		}
		///FOR(i,16) {
		for (i = 0; i < 16; i ++) {
			o[2*i]=(byte) (t[i]&0xff);
			o[2*i+1]=(byte) (t[i]>>8);
		}
	}

	///static int neq25519(gf a, gf b)
	private static int neq25519(long [] a, long [] b)
	{
		byte[] c = new byte[32], d = new byte[32];
		pack25519(c,LongBuffer.wrap(a));
		pack25519(d,LongBuffer.wrap(b));
		return crypto_verify_32(ByteBuffer.wrap(c), ByteBuffer.wrap(d));
	}

	///static byte par25519(gf a)
	static byte par25519(long [] a)
	{
		byte[] d = new byte[32];
		pack25519(d,LongBuffer.wrap(a));
		return (byte) (d[0]&1);
	}

	///private static void unpack25519(gf o, byte []n)
	private static void unpack25519(long [] o, byte [] n)
	{
		int i;
		///FOR(i,16) 
		for (i = 0; i < 16; i ++) o[i]=n[2*i]+((long)n[2*i+1]<<8);
		o[15]&=0x7fff;
	}

	///private static void A(gf o,gf a,gf b)
	private static void A(LongBuffer o, LongBuffer a, LongBuffer b)
	{
		int i;
		///FOR(i,16) 
		for (i = 0; i < 16; i ++) o.put(i, a.get(i) + b.get(i));///o[i]=a[i]+b[i];
	}

	private static void Z(LongBuffer o, LongBuffer a, LongBuffer b)
	{
		int i;
		///FOR(i,16) 
		for (i = 0; i < 16; i ++) o.put(i, a.get(i) - b.get(i));///o[i]=a[i]-b[i];
	}

	private static void M(LongBuffer o, LongBuffer a, LongBuffer b)
	{
		int i,j;
		long [] t = new long[31];
		///FOR(i,31) 
		for (i = 0; i < 31; i ++) t[i]=0;
		///FOR(i,16) FOR(j,16) t[i+j]+=a[i]*b[j];
		for (i = 0; i < 16; i ++) for (j = 0; j < 16; j ++) t[i+j]+=a.get(i)*b.get(j);///a[i]*b[j];
		///FOR(i,15) 
		for (i = 0; i < 15; i ++) t[i]+=38*t[i+16];
		///FOR(i,16) 
		for (i = 0; i < 16; i ++) o.put(i, t[i]);///o[i]=t[i];
		car25519(o);
		car25519(o);
	}

	private static void S(LongBuffer o, LongBuffer a)
	{
		M(o,a,a);
	}

	///private static void inv25519(long [] o,long [] i)
	private static void inv25519(LongBuffer o, LongBuffer i)
	{
		///gf c;
		long [] c = new long[16];
		int a;
		///FOR(a,16) 
		for (a = 0; a < 16; a ++) c[a]=i.get(a);///i[a];
		for(a=253;a>=0;a--) {
			S(LongBuffer.wrap(c),LongBuffer.wrap(c));
			if(a!=2&&a!=4) M(LongBuffer.wrap(c),LongBuffer.wrap(c),i);
		}
		///FOR(a,16) 
		for (a = 0; a < 16; a ++) o.put(a, c[a]);//o[a]=c[a];
	}

	private static void pow2523(long [] o,long [] i)
	{
		///gf c;
		long [] c = new long[16];
		int a;
		///FOR(a,16) 
		for (a = 0; a < 16; a ++) c[a]=i[a];
		for(a=250;a>=0;a--) {
			S(LongBuffer.wrap(c),LongBuffer.wrap(c));
			if(a!=1) M(LongBuffer.wrap(c),LongBuffer.wrap(c),LongBuffer.wrap(i));
		}
		///FOR(a,16) 
		for (a = 0; a < 16; a ++) o[a]=c[a];
	}

	int crypto_scalarmult(byte []q,byte []n,byte []p)
	{
		byte[] z = new byte[32];
		long[] x = new long[80];
		int r,i;

		///gf a,b,c,d,e,f;
		long [] a = new long[16], b = new long[16], c = new long[16],
				d = new long[16], e = new long[16], f = new long[16];
		LongBuffer ab = LongBuffer.wrap(a);
		LongBuffer bb = LongBuffer.wrap(b);
		LongBuffer cb = LongBuffer.wrap(c);
		LongBuffer db = LongBuffer.wrap(d);
		LongBuffer eb = LongBuffer.wrap(e);
		LongBuffer fb = LongBuffer.wrap(f);

		LongBuffer xb = LongBuffer.wrap(x);
		LongBuffer _121665b = LongBuffer.wrap(_121665);

		///FOR(i,31) 
		for (i = 0; i < 31; i ++) z[i]=n[i];
		z[31]=(byte) ((n[31]&127)|64);
		z[0]&=248;
		unpack25519(x,p);
		///FOR(i,16) {
		for (i = 0; i < 16; i ++) {
			b[i]=x[i];
			d[i]=a[i]=c[i]=0;
		}
		a[0]=d[0]=1;

		for(i=254;i>=0;--i) {
			r=(z[i>>3]>>(i&7))&1;
			sel25519(ab,bb,r);
			sel25519(cb,db,r);
			A(eb,ab,cb);
			Z(ab,ab,cb);
			A(cb,bb,db);
			Z(bb,bb,db);
			S(db,eb);
			S(fb,ab);
			M(ab,cb,ab);
			M(cb,bb,eb);
			A(eb,ab,cb);
			Z(ab,ab,cb);
			S(bb,ab);
			Z(cb,db,fb);
			M(ab,cb,_121665b);
			A(ab,ab,db);
			M(cb,cb,ab);
			M(ab,db,fb);
			M(db,bb,xb);
			S(bb,eb);
			sel25519(ab,bb,r);
			sel25519(cb,db,r);
		}
		///FOR(i,16) {
		for (i = 0; i < 16; i ++) {
			x[i+16]=a[i];
			x[i+32]=c[i];
			x[i+48]=b[i];
			x[i+64]=d[i];
		}
		inv25519(LongBuffer.wrap(x, 32, x.length-32)/*x+32*/,LongBuffer.wrap(x, 32, x.length-32)/*x+32*/);
		M(LongBuffer.wrap(x, 16, x.length-16)/*x+16*/,LongBuffer.wrap(x, 16, x.length-16)/*x+16*/,LongBuffer.wrap(x, 32, x.length-32)/*x+32*/);
		pack25519(q,LongBuffer.wrap(x, 16, x.length-16)/*x+16*/);
		return 0;
	}

	int crypto_scalarmult_base(byte []q,byte []n)
	{ 
		return crypto_scalarmult(q,n,_9);
	}

	int crypto_box_keypair(byte [] y, byte [] x)
	{
		randombytes(x,32);
		return crypto_scalarmult_base(y,x);
	}

	int crypto_box_beforenm(byte []k,byte []y,byte []x)
	{
		byte[] s = new byte[32];
		crypto_scalarmult(s,x,y);
		return crypto_core_hsalsa20(k,_0,s,sigma);
	}

	int crypto_box_afternm(byte []c,byte []m,long d,byte []n,byte []k)
	{
		return crypto_secretbox(c,m,d,n,k);
	}

	int crypto_box_open_afternm(byte []m,byte []c,long d,byte []n,byte []k)
	{
		return crypto_secretbox_open(m,c,d,n,k);
	}

	int crypto_box(byte []c,byte []m,long d,byte []n,byte []y,byte []x)
	{
		byte[] k = new byte[32];
		crypto_box_beforenm(k,y,x);
		return crypto_box_afternm(c,m,d,n,k);
	}

	int crypto_box_open(byte []m,byte []c,long d,byte []n,byte []y,byte []x)
	{
		byte[] k = new byte[32];
		crypto_box_beforenm(k,y,x);
		return crypto_box_open_afternm(m,c,d,n,k);
	}

	private static long R(long x,int c) { return (x >> c) | (x << (64 - c)); }
	private static long Ch(long x,long y,long z) { return (x & y) ^ (~x & z); }
	private static long Maj(long x,long y,long z) { return (x & y) ^ (x & z) ^ (y & z); }
	private static long Sigma0(long x) { return R(x,28) ^ R(x,34) ^ R(x,39); }
	private static long Sigma1(long x) { return R(x,14) ^ R(x,18) ^ R(x,41); }
	private static long sigma0(long x) { return R(x, 1) ^ R(x, 8) ^ (x >> 7); }
	private static long sigma1(long x) { return R(x,19) ^ R(x,61) ^ (x >> 6); }

	private static long K[] = 
		{
		0x428a2f98d728ae22L, 0x7137449123ef65cdL, 0xb5c0fbcfec4d3b2fL, 0xe9b5dba58189dbbcL,
		0x3956c25bf348b538L, 0x59f111f1b605d019L, 0x923f82a4af194f9bL, 0xab1c5ed5da6d8118L,
		0xd807aa98a3030242L, 0x12835b0145706fbeL, 0x243185be4ee4b28cL, 0x550c7dc3d5ffb4e2L,
		0x72be5d74f27b896fL, 0x80deb1fe3b1696b1L, 0x9bdc06a725c71235L, 0xc19bf174cf692694L,
		0xe49b69c19ef14ad2L, 0xefbe4786384f25e3L, 0x0fc19dc68b8cd5b5L, 0x240ca1cc77ac9c65L,
		0x2de92c6f592b0275L, 0x4a7484aa6ea6e483L, 0x5cb0a9dcbd41fbd4L, 0x76f988da831153b5L,
		0x983e5152ee66dfabL, 0xa831c66d2db43210L, 0xb00327c898fb213fL, 0xbf597fc7beef0ee4L,
		0xc6e00bf33da88fc2L, 0xd5a79147930aa725L, 0x06ca6351e003826fL, 0x142929670a0e6e70L,
		0x27b70a8546d22ffcL, 0x2e1b21385c26c926L, 0x4d2c6dfc5ac42aedL, 0x53380d139d95b3dfL,
		0x650a73548baf63deL, 0x766a0abb3c77b2a8L, 0x81c2c92e47edaee6L, 0x92722c851482353bL,
		0xa2bfe8a14cf10364L, 0xa81a664bbc423001L, 0xc24b8b70d0f89791L, 0xc76c51a30654be30L,
		0xd192e819d6ef5218L, 0xd69906245565a910L, 0xf40e35855771202aL, 0x106aa07032bbd1b8L,
		0x19a4c116b8d2d0c8L, 0x1e376c085141ab53L, 0x2748774cdf8eeb99L, 0x34b0bcb5e19b48a8L,
		0x391c0cb3c5c95a63L, 0x4ed8aa4ae3418acbL, 0x5b9cca4f7763e373L, 0x682e6ff3d6b2b8a3L,
		0x748f82ee5defb2fcL, 0x78a5636f43172f60L, 0x84c87814a1f0ab72L, 0x8cc702081a6439ecL,
		0x90befffa23631e28L, 0xa4506cebde82bde9L, 0xbef9a3f7b2c67915L, 0xc67178f2e372532bL,
		0xca273eceea26619cL, 0xd186b8c721c0c207L, 0xeada7dd6cde0eb1eL, 0xf57d4f7fee6ed178L,
		0x06f067aa72176fbaL, 0x0a637dc5a2c898a6L, 0x113f9804bef90daeL, 0x1b710b35131c471bL,
		0x28db77f523047d84L, 0x32caab7b40c72493L, 0x3c9ebe0a15c9bebcL, 0x431d67c49c100d4cL,
		0x4cc5d4becb3e42b6L, 0x597f299cfc657e2aL, 0x5fcb6fab3ad6faecL, 0x6c44198c4a475817L
		};

	// TBD... long length n
	///int crypto_hashblocks(byte [] x, byte [] m, long n)
	int crypto_hashblocks(byte [] x, ByteBuffer m, int n)
	{
		long [] z = new long [8], b = new long [8], a = new long [8], w = new long [16];
		long t;
		int i,j;

		///FOR(i,8)
		for (i = 0; i < 8; i ++) z[i] = a[i] = dl64(ByteBuffer.wrap(x, 8*i, x.length-8*i)/*x + 8 * i*/);

		byte[] marray = m.array();
		int moffset = m.arrayOffset();
		while (n >= 128) {
			///FOR(i,16) 
			for (i = 0; i < 16; i ++) w[i] = dl64(ByteBuffer.wrap(marray, 8*i+moffset, marray.length-8*i-moffset)/*m + 8 * i*/);

			///FOR(i,80) {
			for (i = 0; i < 80; i ++) {
				///FOR(j,8) 
				for (j = 0; j < 8; j ++) b[j] = a[j];
				t = a[7] + Sigma1(a[4]) + Ch(a[4],a[5],a[6]) + K[i] + w[i%16];
				b[7] = t + Sigma0(a[0]) + Maj(a[0],a[1],a[2]);
				b[3] += t;
				///FOR(j,8)
				for (j = 0; j < 8; j ++) a[(j+1)%8] = b[j];
				if (i%16 == 15)
					///FOR(j,16)
					for (j = 0; j < 16; j ++)
						w[j] += w[(j+9)%16] + sigma0(w[(j+1)%16]) + sigma1(w[(j+14)%16]);
			}

			///FOR(i,8) 
			for (i = 0; i < 8; i ++) { a[i] += z[i]; z[i] = a[i]; }

			///m += 128;
			moffset += 128;
			n -= 128;
		}

		///FOR(i,8) 
		for (i = 0; i < 8; i ++) ts64(ByteBuffer.wrap(x,8*i,x.length-8*i)/*x+8*i*/,z[i]);

		return n;
	}

	private static byte iv[] = {
		0x6a,0x09,(byte) 0xe6,0x67,(byte) 0xf3,(byte) 0xbc,(byte) 0xc9,0x08,
		(byte) 0xbb,0x67,(byte) 0xae,(byte) 0x85,(byte) 0x84,(byte) 0xca,(byte) 0xa7,0x3b,
		0x3c,0x6e,(byte) 0xf3,0x72,(byte) 0xfe,(byte) 0x94,(byte) 0xf8,0x2b,
		(byte) 0xa5,0x4f,(byte) 0xf5,0x3a,0x5f,0x1d,0x36,(byte) 0xf1,
		0x51,0x0e,0x52,0x7f,(byte) 0xad,(byte) 0xe6,(byte) 0x82,(byte) 0xd1,
		(byte) 0x9b,0x05,0x68,(byte) 0x8c,0x2b,0x3e,0x6c,0x1f,
		0x1f,(byte) 0x83,(byte) 0xd9,(byte) 0xab,(byte) 0xfb,0x41,(byte) 0xbd,0x6b,
		0x5b,(byte) 0xe0,(byte) 0xcd,0x19,0x13,0x7e,0x21,0x79
	} ;

	// TBD 64bits of n
	///int crypto_hash(byte [] out, byte [] m, long n)
	int crypto_hash(byte [] out, ByteBuffer m, int n)
	{
		byte[] h = new byte[64], x = new byte [256];
		int /*long*/ i,b = n;
		///FOR(i,64)
		for (i = 0; i < 64; i ++) h[i] = iv[i];

		crypto_hashblocks(h,m,n);
		///m += n;
		n &= 127;
		///m -= n;

		///FOR(i,256)
		for (i = 0; i < 256; i ++) x[i] = 0;
		///FOR(i,n) 
		for (i = 0; i < n; i ++) x[i] = m.get(i);///m[i];
		x[n] = (byte) 128;

		n = 256-128*(n<112?1:0);
		x[n-9] = (byte) (b >> 61);
		ts64(ByteBuffer.wrap(x,n-8,x.length-(n-8))/*x+n-8*/,b<<3);
		crypto_hashblocks(h,ByteBuffer.wrap(x),n);

		///FOR(i,64) 
		for (i = 0; i < 64; i ++) out[i] = h[i];

		return 0;
	}

	// gf: long[16]
	///private static void add(gf p[4],gf q[4])
	private static void add(long [] p[], long [] q[])
	{
		///gf a,b,c,d,t,e,f,g,h;
		LongBuffer a = LongBuffer.allocate(16);
		LongBuffer b = LongBuffer.allocate(16);
		LongBuffer c = LongBuffer.allocate(16);
		LongBuffer d = LongBuffer.allocate(16);
		LongBuffer t = LongBuffer.allocate(16);
		LongBuffer e = LongBuffer.allocate(16);
		LongBuffer f = LongBuffer.allocate(16);
		LongBuffer g = LongBuffer.allocate(16);
		LongBuffer h = LongBuffer.allocate(16);

		LongBuffer pb0 = LongBuffer.wrap(p[0]);
		LongBuffer pb1 = LongBuffer.wrap(p[1]);
		LongBuffer pb2 = LongBuffer.wrap(p[2]);
		LongBuffer pb3 = LongBuffer.wrap(p[3]);

		LongBuffer qb0 = LongBuffer.wrap(q[0]);
		LongBuffer qb1 = LongBuffer.wrap(q[1]);
		LongBuffer qb2 = LongBuffer.wrap(q[2]);
		LongBuffer qb3 = LongBuffer.wrap(q[3]);

		LongBuffer D2b = LongBuffer.wrap(D2);

		Z(a, pb1/*p[1]*/, pb0/*p[0]*/);
		Z(t, qb1/*q[1]*/, qb0/*q[0]*/);
		M(a, a, t);
		A(b, pb0/*p[0]*/, pb1/*p[1]*/);
		A(t, qb0/*q[0]*/, qb1/*q[1]*/);
		M(b, b, t);
		M(c, pb3/*p[3]*/, qb3/*q[3]*/);
		M(c, c, D2b);
		M(d, pb2/*p[2]*/, qb2/*q[2]*/);
		A(d, d, d);
		Z(e, b, a);
		Z(f, d, c);
		A(g, d, c);
		A(h, b, a);

		M(pb0/*p[0]*/, e, f);
		M(pb1/*p[1]*/, h, g);
		M(pb2/*p[2]*/, g, f);
		M(pb3/*p[3]*/, e, h);
	}

	///private static void cswap(gf p[4],gf q[4],byte b)
	private static void cswap(long [] p[], long [] q[], byte b)
	{
		int i;
		///FOR(i,4)
		for (i = 0; i < 4; i ++)
			///sel25519(p[i], q[i], b);
			sel25519(LongBuffer.wrap(p[i]), LongBuffer.wrap(q[i]), b);  
	}

	///private static void pack(byte []r, gf p[4])
	private static void pack(byte [] r, long [] p[])
	{
		///gf tx, ty, zi;
		LongBuffer tx = LongBuffer.allocate(16);
		LongBuffer ty = LongBuffer.allocate(16);
		LongBuffer zi = LongBuffer.allocate(16);

		inv25519(zi, LongBuffer.wrap(p[2])/*p[2]*/); 
		M(tx, LongBuffer.wrap(p[0])/*p[0]*/, zi);
		M(ty, LongBuffer.wrap(p[1])/*p[1]*/, zi);
		pack25519(r, ty);
		r[31] ^= par25519(tx.array()) << 7;
	}

	///private static void scalarmult(gf p[4],gf q[4],byte []s)
	private static void scalarmult(long [] p[], long [] q[], ByteBuffer s)
	{
		int i;

		set25519(p[0],gf0);
		set25519(p[1],gf1);
		set25519(p[2],gf1);
		set25519(p[3],gf0);

		for (i = 255;i >= 0;--i) {
			byte b = (byte) ((s.get(i/8)>>(i&7))&1);///(byte) ((s[i/8]>>(i&7))&1);
			cswap(p,q,b);
			add(q,p);
			add(p,p);
			cswap(p,q,b);
		}
	}

	///private static void scalarbase(gf p[4],byte []s)
	private static void scalarbase(long [] p[], ByteBuffer s)
	{
		///gf q[4];
		long [] [] q = new long [4] [];
		q[0] = new long [16];
		q[1] = new long [16];
		q[2] = new long [16];
		q[3] = new long [16];

		set25519(q[0],X);
		set25519(q[1],Y);
		set25519(q[2],gf1);
		M(LongBuffer.wrap(q[3]),LongBuffer.wrap(X),LongBuffer.wrap(Y));
		scalarmult(p,q,s);
	}

	int crypto_sign_keypair(byte [] pk, byte [] sk)
	{
		byte[] d = new byte[64];
		///gf p[4];
		long [] [] p = new long [4] [];
		p[0] = new long [16];
		p[1] = new long [16];
		p[2] = new long [16];
		p[3] = new long [16];

		int i;

		randombytes(sk, 32);
		crypto_hash(d, ByteBuffer.wrap(sk), 32);
		d[0] &= 248;
		d[31] &= 127;
		d[31] |= 64;

		scalarbase(p,ByteBuffer.wrap(d));
		pack(pk,p);

		///FOR(i,32) 
		for (i = 0; i < 32; i ++) sk[32 + i] = pk[i];
		return 0;
	}

	private static long L[] = {0xed, 0xd3, 0xf5, 0x5c, 0x1a, 0x63, 0x12, 0x58, 0xd6, 0x9c, 0xf7, 0xa2, 0xde, 0xf9, 0xde, 0x14, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0x10};

	///private static void modL(byte []r, long x[64])
	private static void modL(ByteBuffer r, long x[])
	{
		long carry;///,i,j;
		int i, j;
		for (i = 63;i >= 32;--i) {
			carry = 0;
			for (j = i - 32;j < i - 12;++j) {
				x[j] += carry - 16 * x[i] * L[j - (i - 32)];
				carry = (x[j] + 128) >> 8;
			x[j] -= carry << 8;
			}
			x[j] += carry;
			x[i] = 0;
		}
		carry = 0;
		///FOR(j,32) {
		for (j = 0; j < 32; j ++) {
			x[j] += carry - (x[31] >> 4) * L[j];
			carry = x[j] >> 8;
			x[j] &= 255;
		}
		///FOR(j,32) 
		for (j = 0; j < 32; j ++) x[j] -= carry * L[j];
		///FOR(i,32) {
		for (i = 0; i < 32; i ++) {
			x[i+1] += x[i] >> 8;
		///r[i] = (byte) (x[i] & 255);
		r.put(i, (byte) (x[i] & 255));
		}
	}

	private static void reduce(byte [] r)
	{
		long[] x = new long [64];///,i;
		int i;
		///FOR(i,64)
		for (i = 0; i < 64; i ++) x[i] = (long) r[i];
		///FOR(i,64)
		for (i = 0; i < 64; i ++) r[i] = 0;
		modL(ByteBuffer.wrap(r),x);
	}

	// TBD... 64bits of n
	///int crypto_sign(byte [] sm, long * smlen, byte [] m, long n, byte [] sk)
	int crypto_sign(byte [] sm, BasicBean<Integer> smlen/*long * smlen*/, byte [] m, int/*long*/ n, byte [] sk)
	{
		byte[] d = new byte[64], h = new byte[64], r = new byte[64];

		///long i,j,x[64];
		int i, j;
		long [] x = new long[64];

		///gf p[4];
		long [] [] p = new long [4] [];
		p[0] = new long [16];
		p[1] = new long [16];
		p[2] = new long [16];
		p[3] = new long [16];

		crypto_hash(d, ByteBuffer.wrap(sk), 32);
		d[0] &= 248;
		d[31] &= 127;
		d[31] |= 64;

		///*smlen = n+64;
		smlen.set(n+64);

		///FOR(i,n) 
		for (i = 0; i < n; i ++) sm[64 + i] = m[i];
		///FOR(i,32)
		for (i = 0; i < 32; i ++) sm[32 + i] = d[32 + i];

		crypto_hash(r, ByteBuffer.wrap(sm, 32, sm.length-32)/*sm+32*/, n+32);
		reduce(r);
		scalarbase(p,ByteBuffer.wrap(r));
		pack(sm,p);

		///FOR(i,32)
		for (i = 0; i < 32; i ++) sm[i+32] = sk[i+32];
		crypto_hash(h,ByteBuffer.wrap(sm),n + 64);
		reduce(h);

		///FOR(i,64) 
		for (i = 0; i < 64; i ++) x[i] = 0;
		///FOR(i,32) 
		for (i = 0; i < 32; i ++) x[i] = (long) r[i];
		///FOR(i,32) FOR(j,32) 
		for (i = 0; i < 32; i ++) for (j = 0; j < 32; j ++) x[i+j] += h[i] * (long) d[j];
		modL(ByteBuffer.wrap(sm, 32, sm.length-32)/*sm+32*/,x);

		return 0;
	}

	///static int unpackneg(gf r[4],byte p[32])
	private static int unpackneg(long [] r[], byte p[])
	{
		///gf t, chk, num, den, den2, den4, den6;
		LongBuffer t = LongBuffer.allocate(16);
		LongBuffer chk = LongBuffer.allocate(16);
		LongBuffer num = LongBuffer.allocate(16);
		LongBuffer den = LongBuffer.allocate(16);
		LongBuffer den2 = LongBuffer.allocate(16);
		LongBuffer den4 = LongBuffer.allocate(16);
		LongBuffer den6 = LongBuffer.allocate(16);

		set25519(r[2],gf1);
		unpack25519(r[1],p);
		S(num,LongBuffer.wrap(r[1]));
		M(den,num,LongBuffer.wrap(D)/*D*/);
		Z(num,num,LongBuffer.wrap(r[2]));
		A(den,LongBuffer.wrap(r[2]),den);

		S(den2,den);
		S(den4,den2);
		M(den6,den4,den2);
		M(t,den6,num);
		M(t,t,den);

		pow2523(t.array(),t.array());
		M(t,t,num);
		M(t,t,den);
		M(t,t,den);
		M(LongBuffer.wrap(r[0]),t,den);

		S(chk,LongBuffer.wrap(r[0]));
		M(chk,chk,den);
		if (neq25519(chk.array(), num.array())!=0) M(LongBuffer.wrap(r[0]),LongBuffer.wrap(r[0]),LongBuffer.wrap(I));

		S(chk,LongBuffer.wrap(r[0]));
		M(chk,chk,den);
		if (neq25519(chk.array(), num.array())!=0) return -1;

		if (par25519(r[0]) == (p[31]>>7)) Z(LongBuffer.wrap(r[0]),LongBuffer.wrap(gf0)/*gf0*/,LongBuffer.wrap(r[0])/*r[0]*/);

		M(LongBuffer.wrap(r[3]),LongBuffer.wrap(r[0]),LongBuffer.wrap(r[1]));
		return 0;
	}

	/// TBD 64bits of mlen
	///int crypto_sign_open(byte []m,long *mlen,byte []sm,long n,byte []pk)
	int crypto_sign_open(byte [] m, BasicBean<Integer> mlen/*long *mlen*/, byte [] sm, int/*long*/ n, byte []pk)
	{
		int i;
		byte[] t = new byte[32], h = new byte[64];
		///gf p[4],q[4];
		long [] [] p = new long [4] [];
		p[0] = new long [16];
		p[1] = new long [16];
		p[2] = new long [16];
		p[3] = new long [16];

		long [] [] q = new long [4] [];
		q[0] = new long [16];
		q[1] = new long [16];
		q[2] = new long [16];
		q[3] = new long [16];

		///*mlen = -1;
		mlen.set(-1);

		if (n < 64) return -1;

		if (unpackneg(q,pk)!=0) return -1;

		///FOR(i,n)
		for (i = 0; i < n; i ++) m[i] = sm[i];

		///FOR(i,32) 
		for (i = 0; i < 32; i ++) m[i+32] = pk[i];

		crypto_hash(h,ByteBuffer.wrap(m),n);

		reduce(h);
		scalarmult(p,q,ByteBuffer.wrap(h));

		scalarbase(q,ByteBuffer.wrap(sm, 32, sm.length-32)/*sm + 32*/);
		add(p,q);
		pack(t,p);

		n -= 64;
		if (crypto_verify_32(ByteBuffer.wrap(sm), ByteBuffer.wrap(t))!=0) {
			///FOR(i,n) 
			for (i = 0; i < n; i ++) m[i] = 0;
			return -1;
		}

		///FOR(i,n) 
		for (i = 0; i < n; i ++) m[i] = sm[i + 64];
		///*mlen = n;
		mlen.set(n);
		return 0;
	}

	// TBD...
	private static void randombytes(byte [] x, int len) {
		
	}

}
