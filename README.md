# A5 — Regla de Simpson

Aplicación Java que aproxima la integral de la densidad t de Student entre los
límites `a` y `x` usando la regla compuesta de Simpson. Duplica el número par
de segmentos hasta que dos aproximaciones consecutivas difieren como máximo
en `E = 0.00001`.

## Requisitos

- JDK 21
- Maven 3.8 o posterior

## Compilar y ejecutar

Desde la carpeta raíz de este proyecto (`junit/a5`):

```sh
mvn --batch-mode package
java -cp target/classes junit.a5.App
```

Sin argumentos, la aplicación solicita estos tres datos en la consola:

1. `a`: límite inferior de integración.
2. `x`: límite superior de integración.
3. `dof`: grados de libertad.

El número inicial de segmentos es `NUM_SEG = 10`, definido como constante en
`App`; no se solicita al ejecutar. El cálculo puede duplicarlo para alcanzar
la tolerancia.

También se aceptan como argumentos; el archivo de salida es opcional:

```sh
java -cp target/classes junit.a5.App 0 1.1 9
java -cp target/classes junit.a5.App 1.0 1.1 9
java -cp target/classes junit.a5.App 1.0 1.1 9 custom-result.txt
```

El resultado se imprime en consola y se escribe como un único número en
`a5-results.txt` (o en el archivo opcional indicado).

## Pruebas y cobertura

Ejecutar las pruebas JUnit:

```sh
mvn --batch-mode test
```

Generar el reporte HTML de JaCoCo y comprobar el 100% de instrucciones, ramas
y líneas:

```sh
mvn --batch-mode verify
xdg-open target/site/jacoco/index.html
```

`verify` falla si una modificación hace que cualquiera de esos tres valores
baje del 100%.

## GitHub Actions

El workflow `.github/workflows/java-ci.yml` ejecuta `mvn --batch-mode verify`
al subir cambios a `main`, `master` o `develop` y al abrir un pull request hacia
`main` o `master`. Guarda el reporte JaCoCo como artefacto de la ejecución.

Para publicar esta carpeta como un repositorio independiente:

1. En GitHub, crea un repositorio vacío llamado `a5`. No agregues README,
   `.gitignore` ni licencia desde GitHub.
2. En una terminal, desde la carpeta que contiene `junit/a5`, ejecuta:

   ```sh
   cd junit/a5
   git init -b main
   git add .
   git commit -m "Add Simpson integration A5"
   git remote add origin https://github.com/TU_USUARIO/a5.git
   git push -u origin main
   ```

3. Sustituye `TU_USUARIO` por tu nombre de usuario de GitHub. GitHub puede
   pedirte autenticarte durante el `push`.
4. En el repositorio de GitHub, abre la pestaña **Actions** para ver la
   ejecución y descargar el artefacto `jacoco-report` cuando termine.
