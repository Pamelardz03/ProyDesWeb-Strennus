package com.estudiante.strennus_proyweb.repository

import androidx.lifecycle.LiveData
import com.estudiante.strennus_proyweb.DAO.DetalleSesionDao
import com.estudiante.strennus_proyweb.DAO.EjercicioPersonalizadoDao
import com.estudiante.strennus_proyweb.DAO.RutinaDao
import com.estudiante.strennus_proyweb.DAO.SesionDao
import com.estudiante.strennus_proyweb.DAO.UsuarioDao
import com.estudiante.strennus_proyweb.entities.DetalleSesion
import com.estudiante.strennus_proyweb.entities.EjercicioPersonalizado
import com.estudiante.strennus_proyweb.entities.Rutina
import com.estudiante.strennus_proyweb.entities.Sesion
import com.estudiante.strennus_proyweb.entities.Usuario

class AppRepository(
    private val usuarioDao: UsuarioDao,
    private val sesionDao: SesionDao,
    private val detalleSesionDao: DetalleSesionDao,
    private val rutinaDao: RutinaDao,
    private val ejercicioPersonalizadoDao: EjercicioPersonalizadoDao? = null
) {

    suspend fun insertarUsuario(usuario: Usuario) = usuarioDao.insert(usuario)
    suspend fun actualizarUsuario(usuario: Usuario) = usuarioDao.updateUser(usuario)
    suspend fun eliminarUsuario(usuario: Usuario) = usuarioDao.deleteUser(usuario)
    fun obtenerUsuarioPorId(id: Int): LiveData<Usuario> = usuarioDao.getbyID(id)

    suspend fun insertarSesion(sesion: Sesion): Long = sesionDao.insert(sesion)
    suspend fun actualizarSesion(sesion: Sesion) = sesionDao.update(sesion)
    suspend fun eliminarSesion(sesion: Sesion) = sesionDao.delete(sesion)
    fun obtenerSesionesPorUsuario(usuarioId: Int): LiveData<List<Sesion>> = sesionDao.allsesionsbyID(usuarioId)
    fun obtenerUltimasSesiones(usuarioId: Int, limit: Int): LiveData<List<Sesion>> =
        sesionDao.ultimasSesiones(usuarioId, limit)
    fun obtenerSesionPorId(id: Int): LiveData<Sesion> = sesionDao.sesionbyID(id)

    suspend fun insertarDetalle(detalle: DetalleSesion) = detalleSesionDao.insert(detalle)
    suspend fun actualizarDetalle(detalle: DetalleSesion) = detalleSesionDao.update(detalle)
    suspend fun eliminarDetalle(detalle: DetalleSesion) = detalleSesionDao.delete(detalle)
    fun obtenerDetallePorSesion(sesionId: Int): LiveData<List<DetalleSesion>> = detalleSesionDao.obtenerPorSesion(sesionId)
    suspend fun eliminarDetallePorSesion(sesionId: Int) = detalleSesionDao.eliminarPorSesion(sesionId)

    suspend fun insertarRutina(rutina: Rutina) = rutinaDao.insertar(rutina)
    suspend fun actualizarRutina(rutina: Rutina) = rutinaDao.actualizar(rutina)
    suspend fun eliminarRutina(rutina: Rutina) = rutinaDao.eliminar(rutina)
    fun obtenerRutinasPorUsuario(usuarioId: Int): LiveData<List<Rutina>> = rutinaDao.obtenerPorUsuario(usuarioId)
    fun obtenerRutinaPorId(id: Int): LiveData<Rutina> = rutinaDao.obtenerPorId(id)

    suspend fun insertarEjercicioPersonalizado(ejercicio: EjercicioPersonalizado) = ejercicioPersonalizadoDao?.insertar(ejercicio)
    suspend fun obtenerEjerciciosPersonalizados(usuarioId: Int): List<EjercicioPersonalizado> = ejercicioPersonalizadoDao?.obtenerTodos(usuarioId) ?: emptyList()
    fun obtenerEjerciciosPersonalizadosLive(usuarioId: Int): LiveData<List<EjercicioPersonalizado>>? = ejercicioPersonalizadoDao?.obtenerPorUsuario(usuarioId)
}