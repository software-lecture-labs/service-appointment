package com.dgs.servis.dto.parca;

/** Parça isteklerindeki üst sınırlar. Tek yerde durur ki doğrulama mesajları ve testler aynı değeri kullansın. */
public final class ParcaSinirlari {

	/** Tek bir istekte girilebilecek en fazla adet. Fazladan yazılmış bir sıfırı erkenden yakalar. */
	public static final int ADET_UST_SINIRI = 1_000_000;

	private ParcaSinirlari() {
	}

}
